import bodyParser from 'body-parser';
import { X509Certificate } from "crypto";
import dotenv from 'dotenv';
import app from 'express';
import proxy from 'express-http-proxy';
import pino from "pino";

const logger = pino();

logger.info(" LOADING ENV ");
dotenv.config();

const remoteUrl    = process.env.REMOTE_URL    || 'http://localhost:3001';
const localPort    = process.env.LOCAL_PORT    || '3000';
const localContext = process.env.LOCAL_CONTEXT || '/';
const healthPath   = process.env.HEALTH_PATH   || '/health';
const utilApiUrl   = process.env.UTIL_API_URL  || 'http://localhost:3712';

// ---------------------------------------------------------------------------
// Security context — cache en memoria (TTL 5 min, alinea con util-api)
// ---------------------------------------------------------------------------
const SECURITY_CACHE = new Map();
const SECURITY_CACHE_TTL_MS = 5 * 60 * 1000;

function extractUserKey(request) {
    const gcpInfo = request.headers['x-endpoint-api-userinfo'];
    if (gcpInfo) {
        try {
            const payload = JSON.parse(Buffer.from(gcpInfo, 'base64url').toString('utf8'));
            return payload.sub || payload.preferred_username || null;
        } catch {
            logger.warn('Failed to parse X-Endpoint-API-UserInfo');
        }
    }
    const auth = request.headers['authorization'] || '';
    const token = auth.replace(/^Bearer\s+/i, '');
    if (token) {
        try {
            const parts = token.split('.');
            if (parts.length === 3) {
                const payload = JSON.parse(Buffer.from(parts[1], 'base64url').toString('utf8'));
                return payload.sub || payload.preferred_username || null;
            }
        } catch {
            logger.warn('Failed to decode Bearer token');
        }
    }
    if (!decodedAuthKey) {
        const devKey = request.headers['x-user-key'];
        if (devKey) return devKey;
    }
    return null;
}

async function fetchSecurityContext(userKey) {
    const now = Date.now();
    const cached = SECURITY_CACHE.get(userKey);
    if (cached && cached.expiresAt > now) return cached.data;
    try {
        const res = await fetch(`${utilApiUrl}/api/security/user-attributes-by-key/${encodeURIComponent(userKey)}`);
        if (!res.ok) {
            logger.warn({ userKey, status: res.status }, 'util-api user-attributes non-ok');
            return null;
        }
        const body = await res.json();
        const data = body?.data ?? body;
        SECURITY_CACHE.set(userKey, { data, expiresAt: now + SECURITY_CACHE_TTL_MS });
        return data;
    } catch (err) {
        logger.warn({ userKey, err: err.message }, 'util-api user-attributes error');
        return null;
    }
}

function buildSecurityHeaders(context) {
    if (!context) return {};
    const attrs = context.attributes ?? [];
    const valuesFor = (typeKey) =>
        attrs.filter(a => a.typeKey === typeKey).map(a => a.valueKey).filter(Boolean);
    const vendors = valuesFor('ATR001');
    const types   = valuesFor('ATR002');
    const groups  = valuesFor('ATR004');
    return {
        'x-user-vendors': vendors.length ? vendors.join(',') : '',
        'x-user-types':   types.length   ? types.join(',')   : '',
        'x-user-groups':  groups.length  ? groups.join(',')  : '',
    };
}

logger.info(" CONFIGURING AUTH CERTS ");
const authPublicKey = process.env.AUTH_PUBLIC_KEY || '';
let decodedAuthKey = '';
if (authPublicKey) {
  try {
    decodedAuthKey = new X509Certificate(Buffer.from(authPublicKey, "base64")).toString();
    logger.info(" AUTH CERT LOADED SUCCESSFULLY ");
  } catch (error) {
    logger.warn(" AUTH CERT NOT CONFIGURED - JWT validation disabled ");
  }
} else {
  logger.warn(" AUTH_PUBLIC_KEY not set - JWT validation disabled ");
}

const localService = app();

// ADDING CUSTOM PATH FOR HEALTHCHECK
localService.get(healthPath, (request, response) => {
  response.status(200).send({ message: "healthy" });
});

logger.info(" CONFIGURING PROXY ");
const remoteResolver = proxy(remoteUrl, {
  parseReqBody: true,
  proxyReqPathResolver: (request) => {
    const targetPath = request.originalUrl.replace(localContext, "");
    const normalizedPath = targetPath.startsWith("/") ? targetPath : "/" + targetPath;
    return "/api" + normalizedPath;
  },
  proxyReqOptDecorator: async (options, request) => {
    if (request.method.toLowerCase() === "options") return options;
    if (request.url.indexOf(healthPath) >= 0) return options;

    const userKey = extractUserKey(request);
    if (!userKey) {
      logger.warn({ url: request.originalUrl }, 'No userKey — skipping security context');
      return options;
    }

    const context = await fetchSecurityContext(userKey);
    const secHeaders = buildSecurityHeaders(context);

    options.headers = { ...options.headers, 'x-user-key': userKey, ...secHeaders };
    logger.info({ userKey, vendors: secHeaders['x-user-vendors'], types: secHeaders['x-user-types'] }, 'Security context injected');

    return options;
  },
  proxyErrorHandler: (error, response, nextFilter) => {
    if (error && error.code) {
      logger.warn(`ABORTING REQUEST WITH CODE ${error.code}`);
    } else {
      logger.warn(`ABORTING REQUEST WITH UNKNOWN CODE: ${error}`);
    }
    response.status(500);
    response.send();
  }
});

localService.use(localContext, remoteResolver);

// INCREASING MAX PAYLOAD SIZE (registrado DESPUES del proxy para no consumir el body)
const maximumPayloadSize = '66mb';
localService.use(bodyParser.json({ limit: maximumPayloadSize }));
localService.use(bodyParser.raw({ limit: maximumPayloadSize }));
localService.use(bodyParser.urlencoded({ limit: maximumPayloadSize, extended: true }));

// STARTING PROXY
localService.listen(localPort, () => {
  logger.info(`LISTENING ON PORT: ${localPort} CONTEXT: ${localContext} REMOTE: ${remoteUrl}`);
}).on("request", (request, response) => {
  logger.info(`ACCEPTING NEW REQUEST ${request.method}: ${request.originalUrl} - HEADERS: (${Object.entries(request.headers)}) - STATUS: ${response.statusCode}`);
});
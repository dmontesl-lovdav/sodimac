import { X509Certificate } from "crypto";
import dotenv from "dotenv";
import express from "express";
import proxy from "express-http-proxy";
import pino from "pino";

const logger = pino();

logger.info("LOADING ENV");
dotenv.config();

const remoteUrl = process.env.REMOTE_URL || "http://localhost:3001";
const localPort = process.env.PORT || process.env.LOCAL_PORT || "8080"
const localContext = process.env.LOCAL_CONTEXT || "/";
const healthPath = process.env.HEALTH_PATH || "/health";
const maximumPayloadSize = process.env.MAX_BODY_SIZE || "66mb";
const utilApiUrl = process.env.UTIL_API_URL || "http://localhost:3712";

// ---------------------------------------------------------------------------
// Security context
// ---------------------------------------------------------------------------
const SECURITY_CACHE = new Map();
const SECURITY_CACHE_TTL_MS = 5 * 60 * 1000;

function extractUserKey(request) {
    const gcpInfo = request.headers["x-endpoint-api-userinfo"];
    if (gcpInfo) {
        try {
            const payload = JSON.parse(Buffer.from(gcpInfo, "base64url").toString("utf8"));
            return payload.sub || payload.preferred_username || null;
        } catch { logger.warn("Failed to parse X-Endpoint-API-UserInfo"); }
    }
    const auth = request.headers["authorization"] || "";
    const token = auth.replace(/^Bearer\s+/i, "");
    if (token) {
        try {
            const parts = token.split(".");
            if (parts.length === 3) {
                const payload = JSON.parse(Buffer.from(parts[1], "base64url").toString("utf8"));
                return payload.sub || payload.preferred_username || null;
            }
        } catch { logger.warn("Failed to decode Bearer token"); }
    }
    if (!decodedAuthKey) {
        const devKey = request.headers["x-user-key"];
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
        if (!res.ok) { logger.warn({ userKey, status: res.status }, "util-api user-attributes non-ok"); return null; }
        const body = await res.json();
        const data = body?.data ?? body;
        SECURITY_CACHE.set(userKey, { data, expiresAt: now + SECURITY_CACHE_TTL_MS });
        return data;
    } catch (err) {
        logger.warn({ userKey, err: err.message }, "util-api user-attributes error");
        return null;
    }
}

function buildSecurityHeaders(context) {
    if (!context) return {};
    const attrs = context.attributes ?? [];
    const valuesFor = (typeKey) => attrs.filter(a => a.typeKey === typeKey).map(a => a.valueKey).filter(Boolean);
    const vendors = valuesFor("ATR001");
    const types   = valuesFor("ATR002");
    const groups  = valuesFor("ATR004");
    return {
        "x-user-vendors": vendors.length ? vendors.join(",") : "",
        "x-user-types":   types.length   ? types.join(",")   : "",
        "x-user-groups":  groups.length  ? groups.join(",")  : "",
    };
}

logger.info("CONFIGURING AUTH CERTS");
const authPublicKey = process.env.AUTH_PUBLIC_KEY || "";
let decodedAuthKey = "";
if (authPublicKey) {
  try {
    decodedAuthKey = new X509Certificate(Buffer.from(authPublicKey, "base64")).toString();
    logger.info("AUTH CERT LOADED SUCCESSFULLY");
  } catch (error) {
    logger.warn("AUTH CERT NOT CONFIGURED - JWT validation disabled");
  }
} else {
  logger.warn("AUTH_PUBLIC_KEY not set - JWT validation disabled");
}

logger.info("CONFIGURING EXPRESS");
const localService = express();
localService.set("trust proxy", true);

// Parsers (antes de rutas/proxy)
localService.use(express.json({ limit: maximumPayloadSize }));
localService.use(express.raw({ limit: maximumPayloadSize }));
localService.use(express.urlencoded({ limit: maximumPayloadSize, extended: true }));

// Health (antes del proxy)
localService.get('/health', (req, res) => res.status(200).send({ message: 'healthy' }));
//localService.get(healthPath, (req, res) => {
//res.status(200).send({ message: "healthy" });
//});

logger.info("CONFIGURING PROXY");
const remoteResolver = proxy(remoteUrl, {
  parseReqBody: true,
  proxyReqPathResolver: (request) => {
    const targetPath = request.originalUrl.replace(localContext, "");
    return targetPath.startsWith("/") ? targetPath : "/" + targetPath;
  },
  proxyReqOptDecorator: async (options, request) => {
    if (request.method.toLowerCase() === "options") return options;
    if (request.url.includes(healthPath)) return options;

    const userKey = extractUserKey(request);
    if (!userKey) {
        logger.warn({ url: request.originalUrl }, "No userKey — skipping security context");
        return options;
    }

    const context = await fetchSecurityContext(userKey);
    const secHeaders = buildSecurityHeaders(context);

    options.headers = { ...options.headers, "x-user-key": userKey, ...secHeaders };
    logger.info({ userKey, vendors: secHeaders["x-user-vendors"], types: secHeaders["x-user-types"] }, "Security context injected");

    return options;
  },
  proxyErrorHandler: (error, response) => {
    if (error && error.code) {
      logger.warn(`ABORTING REQUEST WITH CODE ${error.code}`);
    } else {
      logger.warn(`ABORTING REQUEST WITH UNKNOWN CODE: ${error}`);
    }
    response.status(500).send();
  }
});

localService.use(localContext, remoteResolver);

// Logs básicos de requests
localService.on("request", (request, response) => {
  const { method, originalUrl } = request;
  logger.info(`REQUEST ${method} ${originalUrl} - STATUS: ${response.statusCode}`);
});

// Start + graceful shutdown
const server = localService
  .listen(localPort, () => {
    logger.info(`LISTENING ON PORT: ${localPort} CONTEXT: ${localContext} REMOTE: ${remoteUrl}`);
  })
  .on("error", (err) => {
    logger.error({ err }, "SERVER FAILED TO START");
    process.exit(1);
  });

const shutdown = (signal) => {
  logger.warn(`${signal} received. Shutting down...`);
  server.close((err) => {
    if (err) {
      logger.error({ err }, "Error on server.close");
      process.exit(1);
    }
    logger.info("HTTP server closed.");
    process.exit(0);
  });
};
process.on("SIGTERM", () => shutdown("SIGTERM"));
process.on("SIGINT", () => shutdown("SIGINT"));
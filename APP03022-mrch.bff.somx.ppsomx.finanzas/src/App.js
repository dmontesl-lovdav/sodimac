
import dotenv from 'dotenv';
import app from 'express';
import proxy from 'express-http-proxy';
import pino from "pino";
import fs from "fs";
import path from "path";
import { fileURLToPath } from "url";

const logger = pino();

logger.info(" LOADING ENV ");
dotenv.config();

const remoteUrl = process.env.REMOTE_URL || 'http://localhost:3001';
const localPort = process.env.LOCAL_PORT || '3000';
const localContext = process.env.LOCAL_CONTEXT || '/';
const healthPath = process.env.HEALTH_PATH || '/health';
const utilApiUrl = process.env.UTIL_API_URL || 'http://localhost:3712';

const __filename = fileURLToPath(import.meta.url);
const __dirname = path.dirname(__filename);
const openapiPath = path.resolve(__dirname, "../cloud-endpoint/openapi.yaml");
const swaggerUiDistPath = path.resolve(__dirname, "../node_modules/swagger-ui-dist");
const swaggerCssPath = path.join(swaggerUiDistPath, "swagger-ui.css");
const swaggerBundlePath = path.join(swaggerUiDistPath, "swagger-ui-bundle.js");
const swaggerPresetPath = path.join(swaggerUiDistPath, "swagger-ui-standalone-preset.js");

const resolveOpenApiVariables = (request, fileContent) => {
    const forwardedHost = request.get("x-forwarded-host")?.split(",")[0]?.trim();
    const requestHost = request.get("host")?.trim();
    const domainOpenApi = process.env.DOMAIN_OPENAPI || forwardedHost || requestHost || "";
    const replacements = {
        DOMAIN_OPENAPI: domainOpenApi,
        KEYCLOAK: process.env.KEYCLOAK || "",
        JWKS_URL: process.env.JWKS_URL || ""
    };

    return fileContent.replace(/\$\{([A-Z_]+)\}/g, (match, variableName) => {
        const value = replacements[variableName];
        return value !== undefined ? value : match;
    });
};

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

// ---------------------------------------------------------------------------
// STM-1403: Security context — decode JWT + util-api lookup + inject headers
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
    const types = valuesFor("ATR002");
    const groups = valuesFor("ATR004");
    return {
        "x-user-vendors": vendors.length ? vendors.join(",") : "",
        "x-user-types": types.length ? types.join(",") : "",
        "x-user-groups": groups.length ? groups.join(",") : "",
    };
}

const localService = app();

const docsAliases = ["/docs", "/ppsomx/backend-finanzas/docs"];
const openapiAliases = ["/openapi.yaml", "/ppsomx/backend-finanzas/openapi.yaml"];
const swaggerCssAliases = ["/swagger-ui.css", "/ppsomx/backend-finanzas/swagger-ui.css"];
const swaggerBundleAliases = ["/swagger-ui-bundle.js", "/ppsomx/backend-finanzas/swagger-ui-bundle.js"];
const swaggerPresetAliases = ["/swagger-ui-standalone-preset.js", "/ppsomx/backend-finanzas/swagger-ui-standalone-preset.js"];

// ADDING CUSTOM PATH FOR HEALTHCHECK
localService.get(healthPath, (request, response) => {
    response.status(200).send({ message: "healthy" });
});

// SERVE OPENAPI YAML
openapiAliases.forEach((route) => {
    localService.get(route, (request, response) => {
        try {
            const fileContent = fs.readFileSync(openapiPath, "utf8");
            const resolvedContent = resolveOpenApiVariables(request, fileContent);
            response.type("application/yaml");
            response.send(resolvedContent);
        } catch (error) {
            logger.error(`FAILED TO READ OPENAPI FILE: ${error}`);
            response.status(500).send({ message: "Unable to load openapi.yaml" });
        }
    });
});

// SERVE SWAGGER UI CSS
swaggerCssAliases.forEach((route) => {
    localService.get(route, (request, response) => {
        try {
            response.sendFile(swaggerCssPath);
        } catch (error) {
            logger.error(`FAILED TO LOAD SWAGGER CSS: ${error}`);
            response.status(500).send("Unable to load Swagger UI CSS");
        }
    });
});

// SERVE SWAGGER UI BUNDLE
swaggerBundleAliases.forEach((route) => {
    localService.get(route, (request, response) => {
        try {
            response.sendFile(swaggerBundlePath);
        } catch (error) {
            logger.error(`FAILED TO LOAD SWAGGER BUNDLE: ${error}`);
            response.status(500).send("Unable to load Swagger UI bundle");
        }
    });
});

// SERVE SWAGGER UI PRESET
swaggerPresetAliases.forEach((route) => {
    localService.get(route, (request, response) => {
        try {
            response.sendFile(swaggerPresetPath);
        } catch (error) {
            logger.error(`FAILED TO LOAD SWAGGER PRESET: ${error}`);
            response.status(500).send("Unable to load Swagger UI preset");
        }
    });
});

// SERVE SWAGGER UI HTML
docsAliases.forEach((route) => {
    localService.get(route, (request, response) => {
        const openapiUrl = "openapi.yaml";
        const swaggerCssUrl = "swagger-ui.css";
        const swaggerBundleUrl = "swagger-ui-bundle.js";
        const swaggerPresetUrl = "swagger-ui-standalone-preset.js";

        logger.info(`SWAGGER HTML URL DEBUG: ${request.originalUrl}`);
        logger.info(`SWAGGER HTML ASSETS => CSS: ${swaggerCssUrl}, BUNDLE: ${swaggerBundleUrl}, PRESET: ${swaggerPresetUrl}, OPENAPI: ${openapiUrl}`);

        response.type("text/html");
        response.send(`
<!DOCTYPE html>
<html lang="en">
<head>
    <meta charset="UTF-8" />
    <meta name="viewport" content="width=device-width, initial-scale=1" />
    <title>Finanzas BFF Swagger</title>
    <link rel="stylesheet" href="${swaggerCssUrl}" />
</head>
<body>
    <div id="swagger-ui"></div>
    <script src="${swaggerBundleUrl}"></script>
    <script src="${swaggerPresetUrl}"></script>
    <script>
        window.onload = () => {
            window.ui = SwaggerUIBundle({
                url: "${openapiUrl}",
                dom_id: "#swagger-ui",
                deepLinking: true,
                presets: [
                    SwaggerUIBundle.presets.apis,
                    SwaggerUIStandalonePreset
                ],
                layout: "BaseLayout"
            });
        };
    </script>
</body>
</html>
        `);
    });
});

logger.info(" CONFIGURING PROXY ");
const remoteResolver = proxy(remoteUrl, {
    parseReqBody: false,
    proxyReqPathResolver: (request) => {
        const targetPath = request.originalUrl.replace(localContext, "");
        const normalizedPath = targetPath.startsWith("/") ? targetPath : "/" + targetPath;
        return "/api" + normalizedPath;
    },
    proxyReqOptDecorator: async (options, request) => {
        if (request.method.toLowerCase() === "options") {
            return options;
        }

        if (request.url.indexOf(healthPath) >= 0) {
            return options;
        }

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
    proxyErrorHandler: (error, response, nextFilter) => {
        if (error?.code) {
            logger.warn(`ABORTING REQUEST WITH CODE ${error.code}`);
        } else {
            logger.warn(`ABORTING REQUEST WITH UNKNOWN CODE: ${error}`);
        }
        response.status(500);
        response.send();
    }
});

localService.use((request, response, next) => {
    logger.info(`RAW URL DEBUG: ${request.originalUrl}`);
    next();
});

localService.use(localContext, remoteResolver);

// STARTING PROXY
localService.listen(localPort, () => {
    logger.info(`LISTENING ON PORT: ${localPort} CONTEXT: ${localContext} REMOTE: ${remoteUrl}`);
    logger.info(`SWAGGER AVAILABLE AT: /docs`);
    logger.info(`SWAGGER AVAILABLE AT: /ppsomx/backend-finanzas/docs`);
    logger.info(`OPENAPI AVAILABLE AT: /openapi.yaml`);
    logger.info(`OPENAPI AVAILABLE AT: /ppsomx/backend-finanzas/openapi.yaml`);
}).on("request", (request, response) => {
    logger.info(`ACCEPTING NEW REQUEST ${request.method}: ${request.originalUrl} - HEADERS: (${Object.entries(request.headers)}) - STATUS: ${response.statusCode}`);
});

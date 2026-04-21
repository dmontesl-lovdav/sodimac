import bodyParser from 'body-parser';
import { X509Certificate } from "crypto";
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

const __filename = fileURLToPath(import.meta.url);
const __dirname = path.dirname(__filename);
const openapiPath = path.resolve(__dirname, "../cloud-endpoint/openapi.yaml");

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
            response.type("application/yaml");
            response.send(fileContent);
        } catch (error) {
            logger.error(`FAILED TO READ OPENAPI FILE: ${error}`);
            response.status(500).send({ message: "Unable to load openapi.yaml" });
        }
    });
});

// SERVE SWAGGER UI CSS
swaggerCssAliases.forEach((route) => {
    localService.get(route, async (request, response) => {
        try {
            const swaggerCssResponse = await fetch("https://unpkg.com/swagger-ui-dist/swagger-ui.css");
            const swaggerCss = await swaggerCssResponse.text();
            response.type("text/css");
            response.send(swaggerCss);
        } catch (error) {
            logger.error(`FAILED TO LOAD SWAGGER CSS: ${error}`);
            response.status(500).send("Unable to load Swagger UI CSS");
        }
    });
});

// SERVE SWAGGER UI BUNDLE
swaggerBundleAliases.forEach((route) => {
    localService.get(route, async (request, response) => {
        try {
            const swaggerBundleResponse = await fetch("https://unpkg.com/swagger-ui-dist/swagger-ui-bundle.js");
            const swaggerBundle = await swaggerBundleResponse.text();
            response.type("application/javascript");
            response.send(swaggerBundle);
        } catch (error) {
            logger.error(`FAILED TO LOAD SWAGGER BUNDLE: ${error}`);
            response.status(500).send("Unable to load Swagger UI bundle");
        }
    });
});

// SERVE SWAGGER UI PRESET
swaggerPresetAliases.forEach((route) => {
    localService.get(route, async (request, response) => {
        try {
            const swaggerPresetResponse = await fetch("https://unpkg.com/swagger-ui-dist/swagger-ui-standalone-preset.js");
            const swaggerPreset = await swaggerPresetResponse.text();
            response.type("application/javascript");
            response.send(swaggerPreset);
        } catch (error) {
            logger.error(`FAILED TO LOAD SWAGGER PRESET: ${error}`);
            response.status(500).send("Unable to load Swagger UI preset");
        }
    });
});

// SERVE SWAGGER UI HTML
docsAliases.forEach((route) => {
    const openapiUrl = route.startsWith("/ppsomx/backend-finanzas")
        ? "/ppsomx/backend-finanzas/openapi.yaml"
        : "/openapi.yaml";

    const swaggerCssUrl = route.startsWith("/ppsomx/backend-finanzas")
        ? "/ppsomx/backend-finanzas/swagger-ui.css"
        : "/swagger-ui.css";

    const swaggerBundleUrl = route.startsWith("/ppsomx/backend-finanzas")
        ? "/ppsomx/backend-finanzas/swagger-ui-bundle.js"
        : "/swagger-ui-bundle.js";

    const swaggerPresetUrl = route.startsWith("/ppsomx/backend-finanzas")
        ? "/ppsomx/backend-finanzas/swagger-ui-standalone-preset.js"
        : "/swagger-ui-standalone-preset.js";

    localService.get(route, (request, response) => {
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

// INCREASING MAX PAYLOAD SIZE
const maximumPayloadSize = '66mb';
localService.use(bodyParser.json({ limit: maximumPayloadSize }));
localService.use(bodyParser.raw({ limit: maximumPayloadSize }));
localService.use(bodyParser.urlencoded({ limit: maximumPayloadSize, extended: true }));

logger.info(" CONFIGURING PROXY ");
const remoteResolver = proxy(remoteUrl, {
    parseReqBody: false,
    proxyReqPathResolver: (request) => {
        const targetPath = request.originalUrl.replace(localContext, "");
        const normalizedPath = targetPath.startsWith("/") ? targetPath : "/" + targetPath;
        return "/api" + normalizedPath;
    },
    proxyReqOptDecorator: (options, request) => {
        if (request.method.toLowerCase() === "options") {
            return options;
        }

        if (request.url.indexOf(healthPath) >= 0) {
            return options;
        }

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
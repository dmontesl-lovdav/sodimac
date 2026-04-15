import bodyParser from 'body-parser';
import { X509Certificate } from "crypto";
import dotenv from 'dotenv';
import app from 'express';
import proxy from 'express-http-proxy';
import pino from "pino";

const logger = pino();

logger.info(" LOADING ENV ");
dotenv.config();

const remoteUrl = process.env.REMOTE_URL || 'http://localhost:3001';
const localPort = process.env.LOCAL_PORT || '3000';
const localContext = process.env.LOCAL_CONTEXT || '/';
const healthPath = process.env.HEALTH_PATH || '/health';

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

localService.use(localContext, remoteResolver);

// STARTING PROXY
localService.listen(localPort, () => {
  logger.info(`LISTENING ON PORT: ${localPort} CONTEXT: ${localContext} REMOTE: ${remoteUrl}`);
}).on("request", (request, response) => {
  logger.info(`ACCEPTING NEW REQUEST ${request.method}: ${request.originalUrl} - HEADERS: (${Object.entries(request.headers)}) - STATUS: ${response.statusCode}`);
});
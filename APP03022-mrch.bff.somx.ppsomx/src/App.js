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
const decodedAuthKey = new X509Certificate(Buffer.from(authPublicKey, "base64")).toString();

logger.info(" CONFIGURING PROXY ");
const remoteResolver = proxy(remoteUrl, {
    parseReqBody: false,
    proxyReqPathResolver: (request) => {
        return request.originalUrl.replace(localContext, "/");
    },
    proxyReqOptDecorator: (options, request) => {
        if (request.method.toLowerCase() === "options") {
            // SKIPING
            return options;
        }
        if (request.url.indexOf(healthPath) >= 0) {
            // SKIPPING
            return options;
        }
        //  try {
        //    const decodedToken = jwt.verify(request.headers.authorization.replace(/.*Bearer /, ""), decodedAuthKey);
        // } catch (error) {
        //   throw { code: 401 };
        // }

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

const localService = app().use(localContext, remoteResolver);

// ADDING CUSTOM PATH FOR HEALTHCHECK
localService.get(healthPath, (request, response) => {
    response.status(200).send({ message: "healthy" });
});

// INCREASING MAX PAYLOAD SIZE
const maximumPayloadSize = '66mb';
localService.use(bodyParser.json({ limit: maximumPayloadSize }));
localService.use(bodyParser.raw({ limit: maximumPayloadSize }));
localService.use(bodyParser.urlencoded({ limit: maximumPayloadSize }));

// STARTING PROXY
localService.listen(localPort, () => {
    logger.info(`LISTENING ON PORT: ${localPort} CONTEXT: ${localContext} REMOTE: ${remoteUrl}`);
}).on("request", (request, response) => {
    logger.info(`ACCEPTING NEW REQUEST ${request.method}: ${request.originalUrl} - HEADERS: (${Object.entries(request.headers)}) - STATUS: ${response.statusCode}`);
});
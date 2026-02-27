import bodyParser from 'body-parser';
import dotenv from 'dotenv';
import express from 'express';
import proxy from 'express-http-proxy';
import pino from "pino";


const logger = pino();

logger.info(" LOADING ENV ");
dotenv.config();
const remoteUrl = process.env.REMOTE_URL || 'http://localhost:8083';
const localPort = process.env.LOCAL_PORT || '3000';
const localContext = process.env.LOCAL_CONTEXT || '/';
const healthPath = process.env.HEALTH_PATH || '/health';

logger.info(" CONFIGURING PROXY ");
const remoteResolver = proxy(remoteUrl, {
    parseReqBody: false,
    proxyReqPathResolver: (request) => {
        const targetPath = request.originalUrl.replace(localContext, "");
        return targetPath.startsWith("/") ? targetPath : "/" + targetPath;
    },
    proxyReqOptDecorator: (options, request) => {
        if (request.method.toLowerCase() === "options") {
            // SKIPING
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

const localService = express();

// INCREASING MAX PAYLOAD SIZE
const maximumPayloadSize = '10mb';
localService.use(bodyParser.json({ limit: maximumPayloadSize }));
localService.use(bodyParser.raw({ limit: maximumPayloadSize }));
localService.use(bodyParser.urlencoded({ limit: maximumPayloadSize, extended: true }));

// PROXY ALL REQUESTS
localService.use(localContext, remoteResolver);

// STARTING PROXY
localService.listen(localPort, () => {
    logger.info(`LISTENING ON PORT: ${localPort} CONTEXT: ${localContext} REMOTE: ${remoteUrl}`);
}).on("request", (request, response) => {
    logger.info(`ACCEPTING NEW REQUEST ${request.method}: ${request.originalUrl} - HEADERS: (${Object.entries(request.headers)}) - STATUS: ${response.statusCode}`);
});

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
  parseReqBody: false,
  proxyReqPathResolver: (request) => {
    const targetPath = request.originalUrl.replace(localContext, "");
    return targetPath.startsWith("/") ? targetPath : "/" + targetPath;
  },
  proxyReqOptDecorator: (options, request) => {
    if (request.method.toLowerCase() === "options") return options;
    if (request.url.includes(healthPath)) return options;

    // // JWT opcional:
    // const token = (request.headers.authorization || "").replace(/.*Bearer /, "");
    // try {
    //   jwt.verify(token, decodedAuthKey);
    // } catch {
    //   throw { code: 401 };
    // }

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
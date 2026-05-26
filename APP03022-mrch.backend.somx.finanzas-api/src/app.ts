import "dotenv/config";
import express from "express";
import helmet from "helmet";
import cors from "cors";

import { mountSwagger } from "./swagger.js";
import router from "./routes/index.js";

import { errorHandler } from "@/middlewares/errorHandler.js";
import { healthCheck, livenessProbe, readinessProbe } from "@/controllers/health.controller.js";
import { globalErrorHandler } from "@/middlewares/logger.js";
import { attachSecurityContext } from "@/middlewares/security.middleware.js";
import { attachAuthToken } from "@/middlewares/authToken.js";

const app = express();

// Cors
app.use(cors());

app.use(helmet());

// Parsers
app.use(express.json());
app.use(express.urlencoded({ extended: false }));

// Swagger
await mountSwagger(app);

// Health check endpoints (Spring Boot Actuator style)
app.get("/health", healthCheck);
app.get("/actuator/health", healthCheck);
app.get("/actuator/health/liveness", livenessProbe);
app.get("/actuator/health/readiness", readinessProbe);

// Security context (lee headers x-user-vendors/types/groups del BFF)
app.use("/api", attachSecurityContext);

// Auth token global para todas las rutas /api
app.use("/api", attachAuthToken);

// Rutas de la API
console.log("[APP] Mounting router at /api, router type:", typeof router, "has stack:", !!(router as any).stack);
app.use("/api", router);
console.log("[APP] Router mounted successfully");

// 404 solo para rutas de la API
app.use("/api", (_req, res) => {
    res.status(404).json({ error: "Not Found" });
});

// Middleware global de errores (SIEMPRE al final)
app.use(errorHandler);
app.use(globalErrorHandler);

export default app;
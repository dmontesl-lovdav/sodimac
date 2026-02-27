import "dotenv/config";
import express from "express";
import helmet from "helmet";
import cors from "cors";

import router from "./routes/index.js";
import { errorHandler } from "@/middlewares/errorHandler.js";
import { globalErrorHandler } from '@/middlewares/logger.js';

const app = express();

// 🧠 Seguridad básica
app.use(helmet());
app.use(cors());

// 🧩 Parsers
app.use(express.json());
app.use(express.urlencoded({ extended: false }));

// ❤️ Health check
app.get("/health", (_req, res) => {
    res.json({ ok: true, env: process.env.NODE_ENV ?? "development" });
});

// 🚏 Rutas de la API
app.use("/api", router);

// 🚫 404 solo para rutas de la API
app.use("/api", (_req, res) => {
    res.status(404).json({ error: "Not Found" });
});

// ⚠️ Middleware global de errores (SIEMPRE al final)
app.use(errorHandler);
app.use(globalErrorHandler); //MANEJO DE ERRROES PARA ESCRITURA EN LA BASE
export default app;

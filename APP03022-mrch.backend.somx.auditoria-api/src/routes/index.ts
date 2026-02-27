// src/routes/index.ts
import { Router, type Router as RouterType } from "express";
import activityLogsRouter from "./activityLogs.routes.js";

const router: RouterType = Router();

// monta las rutas con prefijo
router.use("/activity-logs", activityLogsRouter);

export default router;

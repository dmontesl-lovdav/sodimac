// src/routes/index.ts
import { Router, type Router as RouterType } from "express";
import parameterRouter from "./parameter.routes.js";
import moduleRouter from "./module.routes.js";
import messageRouter from "./message.routes.js";
import applicationMsgRouter from "./applicationMsg.routes.js";
import processRouter from "./process.routes.js";
import itemTypeRouter from "./itemType.routes.js";
import itemRouter from "./item.routes.js";
import activityLogsRouter from "./activityLogs.routes.js";

const router: RouterType = Router();

// Monta las rutas con prefijo
router.use("/parameters", parameterRouter);
router.use("/modules", moduleRouter);
router.use("/messages", messageRouter);
router.use("/application-messages", applicationMsgRouter);
router.use("/processes", processRouter);
router.use("/item-types", itemTypeRouter);
router.use("/items", itemRouter);
router.use("/activity-logs", activityLogsRouter);

export default router;

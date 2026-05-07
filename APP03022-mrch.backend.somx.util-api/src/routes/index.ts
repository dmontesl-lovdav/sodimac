import { Router, type Router as RouterType } from "express";

import parameterRouter from "./parameter.routes.js";
import moduleRouter from "./module.routes.js";
import messageRouter from "./message.routes.js";
import applicationMsgRouter from "./applicationMsg.routes.js";
import processRouter from "./process.routes.js";
import itemTypeRouter from "./itemType.routes.js";
import itemRouter from "./item.routes.js";
import activityLogsRouter from "./activityLogs.routes.js";
import securityRouter from "./security.routes.js";

import catalogRouter from "./catalog.routes.js";
import catalogosRouter from "./catalogos.routes.js";
import conversionRouter from "./conversion.routes.js";
import statusTrainRouter from "./statusTrain.routes.js";
import supplierRouter from "./supplier.routes.js";
import supplierBlockRouter from "./supplierBlock.routes.js";
import validationReportRouter from "./validationReport.routes.js";

const router: RouterType = Router();

router.use("/parameters", parameterRouter);
router.use("/modules", moduleRouter);
router.use("/messages", messageRouter);
router.use("/application-messages", applicationMsgRouter);
router.use("/processes", processRouter);
router.use("/item-types", itemTypeRouter);
router.use("/items", itemRouter);
router.use("/activity-logs", activityLogsRouter);
router.use("/security", securityRouter);

router.use("/catalogos", catalogosRouter);
router.use("/status-train", statusTrainRouter);
router.use("/suppliers", supplierRouter);
router.use("/supplier-blocks", supplierBlockRouter);
router.use("/validation-reports", validationReportRouter);
router.use("/conversions", conversionRouter);

router.use("/catalog", catalogRouter);

export default router;


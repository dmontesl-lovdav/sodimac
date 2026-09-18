import { Router } from "express";
import * as ctrl from "@/controllers/shippingGuide.controller.js";
import { validateBody, validateParams, validateQuery } from "@/middlewares/validate.js";
import {
    IdParamSchema,
    ListShippingGuideQuerySchema,
    UpdateShippingGuideSchema,
    CancelShippingGuidesSchema,
    UpdateShippingGuideStatusSchema,
} from "@/schemas/shippingGuide.schema.js";
import {  DownloadFileSchema} from "@/schemas/storageGcp.schema.js";
import { requirePermission } from "@/middlewares/permission.middleware.js";
import { activityLogger, logBeforeMethod } from "@/middlewares/logger.js";

const router = Router();

const controllerName = "ShippingGuide";

router.use(activityLogger(controllerName));

router.get("/downloadFile", validateQuery(DownloadFileSchema), logBeforeMethod("downloadFile"), ctrl.downloadOneFile);
router.get("/", validateQuery(ListShippingGuideQuerySchema), logBeforeMethod("list"), ctrl.list);
router.get("/csv", validateQuery(ListShippingGuideQuerySchema), logBeforeMethod("csvExport"), ctrl.csvExport);
router.post("/cancel", requirePermission("EVT0017"), validateBody(CancelShippingGuidesSchema), logBeforeMethod("cancel"), ctrl.cancel);
router.post(
    "/status",
    requirePermission("EVT0018"),
    validateBody(UpdateShippingGuideStatusSchema),
    logBeforeMethod("updateStatus"),
    ctrl.updateStatus
);
router.get("/:uuid", validateParams(IdParamSchema), logBeforeMethod("getById"), ctrl.getById);
router.put("/:uuid", requirePermission("EVT0018"), validateBody(UpdateShippingGuideSchema), logBeforeMethod("updateByUuid"), ctrl.updateByUuid);
router.patch("/guide/:idGuide", requirePermission("EVT0018"), validateBody(UpdateShippingGuideSchema), logBeforeMethod("updateByGuide"), ctrl.updateByGuide);
router.delete("/:uuid", validateParams(IdParamSchema), logBeforeMethod("remove"), ctrl.remove);

export default router;
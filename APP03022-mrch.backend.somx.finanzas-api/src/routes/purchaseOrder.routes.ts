import { Router } from "express";
import * as ctrl from "@/controllers/purchaseOrder.controller.js";
import { validateBody, validateParams, validateQuery } from "@/middlewares/validate.js";
import { attachAuthToken } from "@/middlewares/authToken.js";
import { activityLogger, logBeforeMethod } from '@/middlewares/logger.js';
import {
    CreatePurchaseOrderSchema,
    UpdateStatusReceptionSchema,
    UpdateStatusReceptionSchemaByUuid,
    ListPurchaseOrderQuerySchema,
} from "@/schemas/purchaseOrder.schema.js";
import {
    ListReceptionQuerySchema,
} from "@/schemas/reception.schema.js";

const router = Router();


// Aplica el middleware SOLO a este router con su serviceName
const controllerName = 'PurchaseOrders';

router.use(activityLogger(controllerName))
router.use(attachAuthToken)

router.get("/listReception",validateBody(ListReceptionQuerySchema), logBeforeMethod('listReception'), ctrl.listReception)
router.patch("/updateReception",validateBody(UpdateStatusReceptionSchema), logBeforeMethod('updateReception'), ctrl.updateReception)
router.get("/reception/:uuid", logBeforeMethod('getReceptionById'), ctrl.getReceptionById)
router.patch("/reception/:uuid",validateBody(UpdateStatusReceptionSchemaByUuid), logBeforeMethod('updateReceptionStatusByUuid'), ctrl.updateReceptionStatusByUuid)
    
router.post("/",validateBody(CreatePurchaseOrderSchema), logBeforeMethod('save'), ctrl.save)
router.get("/", validateQuery(ListPurchaseOrderQuerySchema), logBeforeMethod('list'), ctrl.list)
router.get("/:uuid", logBeforeMethod('getById'), ctrl.getById)
router.patch("/:uuid", logBeforeMethod('updateById'), ctrl.updateById)

export default router;
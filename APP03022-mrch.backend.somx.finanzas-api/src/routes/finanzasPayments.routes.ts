import { Router, type Router as RouterType } from "express";
import * as controller from "@/controllers/finanzasPayment.controller.js";
import { validateBody, validateParams, validateQuery } from "@/middlewares/validate.js";
import {
    CreateFinanzasPaymentSchema,
    UpdateFinanzasPaymentSchema,
    ListFinanzasPaymentsQuerySchema
} from "@/schemas/finanzasPayment.schema.js";
import { activityLogger   } from '@/middlewares/logger.js';

const r: RouterType = Router();
r.use(activityLogger('FinanzasPayment'));
r.get("/", validateBody(ListFinanzasPaymentsQuerySchema), controller.list);
// r.get("/:uuid", validateParams(IdParamSchema), controller.getById);
 r.post("/", validateBody(CreateFinanzasPaymentSchema), controller.create);
 r.patch("/", validateBody(UpdateFinanzasPaymentSchema), controller.update);
// r.delete("/:uuid", validateParams(IdParamSchema), controller.remove);

export default r;

import { Router, type Router as RouterType } from "express";
import * as controller from "@/controllers/finanzasPayment.controller.js";
import { validateBody, validateParams, validateQuery } from "@/middlewares/validate.js";
import {
    CreateFinanzasPaymentSchema,
    UpdateFinanzasPaymentSchema,
    ListFinanzasPaymentsQuerySchema,
    CreateFinanzasPaymentHeaderWithDetailsSchema,
    PaymentHeaderUuidParamSchema,
} from "@/schemas/finanzasPayment.schema.js";
import { activityLogger } from "@/middlewares/logger.js";

const r: RouterType = Router();

r.use(activityLogger("FinanzasPayment"));

r.get("/", validateQuery(ListFinanzasPaymentsQuerySchema), controller.list);

r.post(
    "/header-with-details",
    validateBody(CreateFinanzasPaymentHeaderWithDetailsSchema),
    controller.createHeaderWithDetails
);

r.post("/", validateBody(CreateFinanzasPaymentSchema), controller.create);
r.patch("/", validateBody(UpdateFinanzasPaymentSchema), controller.update);

r.get(
    "/header-with-details/:paymentHeaderUuid",
    validateParams(PaymentHeaderUuidParamSchema),
    controller.getHeaderWithDetails
);

export default r;
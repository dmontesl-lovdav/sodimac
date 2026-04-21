import { Router, type Router as RouterType } from "express";
import * as controller from "@/controllers/transactionId.controller.js";
import { validateBody, validateParams } from "@/middlewares/validate.js";
import {
    CreateTransactionIdSchema,
    TransactionIdFolioParamSchema,
} from "@/schemas/transactionId.schema.js";

const r: RouterType = Router();

r.post(
    "/",
    validateBody(CreateTransactionIdSchema),
    controller.create
);

r.get(
    "/:folioVisible",
    validateParams(TransactionIdFolioParamSchema),
    controller.detailByFolioVisible
);

export default r;
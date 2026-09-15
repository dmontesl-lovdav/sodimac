import { Router } from "express";
import * as ctrl from "@/controllers/sapDocument.controller.js";
import {
    CreateSapDocumentSchema,
    UpdateSapDocumentSchema,
    ListSapDocumentQuerySchema,
    IdParamSchema,
    FiscalUuidParamSchema,
} from "@/schemas/sapDocument.schema.js";
import { validateBody, validateParams, validateQuery } from "@/middlewares/validate.js";

const router = Router();
router.get("/", validateQuery(ListSapDocumentQuerySchema), ctrl.list);
router.get(
    "/by-fiscal-uuid/:fiscalUuid",
    validateParams(FiscalUuidParamSchema),
    ctrl.listByFiscalUuid
);
router.get("/:uuid", validateParams(IdParamSchema), ctrl.getById);
router.post("/", validateBody(CreateSapDocumentSchema), ctrl.create);
router.put("/:uuid", validateParams(IdParamSchema), validateBody(UpdateSapDocumentSchema), ctrl.update);
router.delete("/:uuid", validateParams(IdParamSchema), ctrl.remove);

export default router;

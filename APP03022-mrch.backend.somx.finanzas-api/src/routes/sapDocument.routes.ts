import { Router } from "express";
import * as ctrl from "@/controllers/sapDocument.controller.js";
import {
    CreateSapDocumentSchema,
    UpdateSapDocumentSchema,
    ListSapDocumentQuerySchema,
    IdParamSchema,
    type CreateSapDocumentDto,
    type UpdateSapDocumentDto,
    type ListSapDocumentQuery,
} from "@/schemas/sapDocument.schema.js";
import { validateBody, validateParams } from "@/middlewares/validate.js";

const router = Router();
router.get("/",validateBody(ListSapDocumentQuerySchema), ctrl.list);
router.get("/:uuid",validateParams(IdParamSchema), ctrl.getById);
router.post("/",validateBody(CreateSapDocumentSchema), ctrl.create);
router.put("/:uuid",validateBody(UpdateSapDocumentSchema), ctrl.update);
router.delete("/:uuid",validateParams(IdParamSchema), ctrl.remove);

export default router;

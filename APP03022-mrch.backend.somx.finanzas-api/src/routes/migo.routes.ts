import { Router, type Router as RouterType } from "express";
import multer from "multer";
import * as controller from "@/controllers/migo.controller.js";
import { validateQuery, validateBody, validateParams } from "@/middlewares/validate.js";
import { requirePermission } from "@/middlewares/permission.middleware.js";
import {
    ListMigoDocumentsQuerySchema,
    ListMigoReceptionsQuerySchema,
    RejectMigoSchema,
    MigoDocumentIdParamSchema,
} from "@/schemas/migo.schema.js";

const upload = multer({ storage: multer.memoryStorage(), limits: { fileSize: 10 * 1024 * 1024 } });

const r: RouterType = Router();

r.get("/", validateQuery(ListMigoDocumentsQuerySchema), controller.listDocuments);

r.get("/:id", validateParams(MigoDocumentIdParamSchema), controller.getDocumentById);

r.get("/:id/receptions", validateParams(MigoDocumentIdParamSchema), validateQuery(ListMigoReceptionsQuerySchema), controller.listReceptions);

r.post("/upload", requirePermission("EVT0038"), upload.single("file"), controller.uploadCsv);

r.patch("/:id/authorize", requirePermission("EVT0042"), validateParams(MigoDocumentIdParamSchema), controller.authorizeDocument);

r.patch("/reject", requirePermission("EVT0043"), validateBody(RejectMigoSchema), controller.rejectDocument);

r.get("/:id/export-csv", validateParams(MigoDocumentIdParamSchema), controller.exportReceptionsCsv);

export default r;

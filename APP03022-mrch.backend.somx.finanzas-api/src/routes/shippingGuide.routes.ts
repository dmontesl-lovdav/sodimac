import { Router } from "express";
import * as ctrl from "@/controllers/shippingGuide.controller.js";
import multer from "multer";
import { validateBody, validateParams, validateQuery } from "@/middlewares/validate.js";
import { attachAuthToken } from "@/middlewares/authToken.js";
import {
    IdParamSchema,
    ListShippingGuideQuerySchema,
    UpdateShippingGuideSchema
} from "@/schemas/shippingGuide.schema.js";
import { activityLogger, logBeforeMethod } from "@/middlewares/logger.js";

const router = Router();

const storage = multer.memoryStorage();
const upload = multer({ storage: storage });

const CreateShippingGuideSchemaError = "";
const controllerName = "ShippingGuide";

router.use(activityLogger(controllerName));
router.use(attachAuthToken);

router.get("/", validateQuery(ListShippingGuideQuerySchema), logBeforeMethod("list"), ctrl.list);
router.get("/csv", validateQuery(ListShippingGuideQuerySchema), logBeforeMethod("csvExport"), ctrl.csvExport);
router.get("/:uuid", validateParams(IdParamSchema), logBeforeMethod("getById"), ctrl.getById);
router.put("/:uuid", validateBody(UpdateShippingGuideSchema), logBeforeMethod("updateByUuid"), ctrl.updateByUuid);
router.patch("/guide/:idGuide", validateBody(UpdateShippingGuideSchema), logBeforeMethod("updateByGuide"), ctrl.updateByGuide);
router.delete("/:uuid", validateParams(IdParamSchema), logBeforeMethod("remove"), ctrl.remove);

export default router;
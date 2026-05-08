import { Router } from "express";
import * as ctrl from "@/controllers/cartaPorte.controller.js";
import multer from 'multer';
import { validateArrayFilesAndCPObj, validateFormData, validateBody } from "@/middlewares/validate.js";
import {
    CreateShippingGuideSchemaList,
    ListShippingGuideQuerySchema,
    ShippginGuideSummaryListSchema
} from "@/schemas/shippingGuide.schema.js";
import {
    CreatePurchaseOrderSchema,
} from "@/schemas/purchaseOrder.schema.js";
import { 
    BaseArrayFilesSchemaParent,
    BaseSchemaParent
} from "@/schemas/base.shema.js";
import { activityLogger, logBeforeMethod } from '@/middlewares/logger.js';
import {uploadMiddleware} from '@/middlewares/upload.Middleware.js'
import { attachAuthToken } from "@/middlewares/authToken.js";

const router = Router();


const CreateShippingGuideSchemaError = "";  


// Aplica el middleware SOLO a este router con su serviceName
const controllerName = 'CartaPorte';
router.use(activityLogger(controllerName));
router.use(attachAuthToken)

router.post("/guia-embarque", uploadMiddleware(2) , validateArrayFilesAndCPObj(CreateShippingGuideSchemaList, BaseArrayFilesSchemaParent), logBeforeMethod( 'guia-embarque') , ctrl.createGuia );
router.post("/oc", uploadMiddleware(0), validateFormData(CreatePurchaseOrderSchema, BaseSchemaParent), logBeforeMethod('oc'), ctrl.createOc);
router.post("/all", uploadMiddleware(2), validateArrayFilesAndCPObj(CreatePurchaseOrderSchema, BaseArrayFilesSchemaParent), logBeforeMethod('all'), ctrl.createall);
router.post("/findAllGuia",validateBody(ListShippingGuideQuerySchema), logBeforeMethod('findAllGuia'), ctrl.findAllGuia);
router.post("/updateAllStatusGuia",validateBody(ShippginGuideSummaryListSchema), logBeforeMethod('updateAllStatusGuia'), ctrl.updateAllStatusGuia);

export default router;

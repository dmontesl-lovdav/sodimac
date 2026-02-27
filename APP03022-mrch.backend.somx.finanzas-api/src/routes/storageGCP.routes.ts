import { Router } from "express";
import * as ctrl from "@/controllers/storageGCP.controller.js";

import { activityLogger, logBeforeMethod   } from '@/middlewares/logger.js';
import {uploadMiddleware} from '@/middlewares/upload.Middleware.js'

const router = Router();


const CreateShippingGuideSchemaError = "";  


// Aplica el middleware SOLO a este router con su serviceName
router.use(activityLogger('StorageGCP'));
router.post("/upload", uploadMiddleware(2), logBeforeMethod('uploadMultiple'), ctrl.uploadMultiple );
router.get("/download", logBeforeMethod('downloadFile'), ctrl.downloadFile );


export default router;

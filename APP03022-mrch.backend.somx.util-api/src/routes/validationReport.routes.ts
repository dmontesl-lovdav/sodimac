import { Router } from 'express';
import * as layoutController from '@/controllers/layoutValidation.controller.js';

const router = Router();

router.get('/:reportId', layoutController.getReport);

export default router;


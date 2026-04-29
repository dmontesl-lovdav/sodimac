import { Router } from 'express';
import * as ctrl from '@/controllers/supplier.controller.js';

const router = Router();

// GET /api/suppliers?supplierNumber=&businessName=&status=1&page=1&pageSize=20
router.get('/', ctrl.listSuppliers);

export default router;

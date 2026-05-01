import { Router } from 'express';
import * as supplierBlockController from '@/controllers/supplierBlock.controller.js';

const router = Router();

router.get('/', supplierBlockController.getAllSupplierBlocks);
router.post('/', supplierBlockController.createSupplierBlock);
router.get('/supplier/:supplierNumber/active', supplierBlockController.getCurrentActiveBlocks);
router.get('/supplier/:supplierNumber/is-blocked', supplierBlockController.isSupplierBlocked);
router.get('/supplier/:supplierNumber/at-date', supplierBlockController.getBlocksAtDate);
router.get('/supplier/:supplierNumber', supplierBlockController.getBlocksBySupplierNumber);
router.get('/:id', supplierBlockController.getSupplierBlockById);
router.put('/:id', supplierBlockController.updateSupplierBlock);
router.delete('/:id', supplierBlockController.deleteSupplierBlock);

export default router;


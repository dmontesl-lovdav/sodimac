import { Router } from 'express';
import * as supplierController from '@/controllers/supplier.controller.js';

const router = Router();

router.get('/', supplierController.getAllSuppliers);
router.post('/', supplierController.createSupplier);
router.get('/types', supplierController.getAllSupplierTypes);
router.get('/payment-conditions', supplierController.getAllPaymentConditions);
router.get('/filter', supplierController.filterSuppliers);
router.get('/number/:supplierNumber/type-blocked', supplierController.getSupplierTypeBlocked);
router.get('/number/:supplierNumber', supplierController.getSupplierByNumber);
router.get('/rfc/:rfc', supplierController.getSupplierByRfc);
router.get('/:id', supplierController.getSupplierById);
router.put('/:id', supplierController.updateSupplier);
router.delete('/:id', supplierController.deleteSupplier);

export default router;


import { Router } from 'express';
import * as managementController from '@/controllers/catalogManagement.controller.js';
import * as elementController from '@/controllers/catalogElement.controller.js';
import * as layoutController from '@/controllers/layoutValidation.controller.js';

const router = Router();

router.post('/validate-layout', layoutController.uploadFile, layoutController.validate);

router.get('/primarios', elementController.getPrimaryCatalogs);
router.patch('/elementos/:elementId/estatus', elementController.changeStatus);
router.get('/:catalogId/elementos/activos', elementController.getActiveElements);
router.get('/:catalogId/elementos/:elementId', elementController.getElementById);
router.put('/:catalogId/elementos/:elementId', elementController.updateElement);
router.get('/:catalogId/elementos', elementController.getElements);
router.post('/:catalogId/elementos', elementController.createElement);
router.get('/:catalogId/detalle', elementController.getCatalogDetail);

router.get('/', managementController.getCatalogs);
router.post('/', managementController.createCatalog);
router.get('/:id', managementController.getCatalogById);
router.put('/:id', managementController.updateCatalog);

export default router;


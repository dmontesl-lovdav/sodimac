import { Router } from 'express';
import * as catalogController from '@/controllers/catalog.controller.js';

const router = Router();

router.get('/id/:id', catalogController.getCatalogById);
router.get('/prefix/:prefix', catalogController.getCatalogByPrefix);
router.get('/message/:key/format', catalogController.getMessageByKeyFormatted);
router.get('/message/:key', catalogController.getMessageByKey);
router.get('/module/:module', catalogController.getCatalogsByModule);
router.get('/:code/details/:key', catalogController.getCatalogDetailByKey);
router.get('/:code/details', catalogController.getCatalogDetails);
router.get('/:code', catalogController.getCatalogByCode);
router.get('/', catalogController.getAllCatalogs);

export default router;


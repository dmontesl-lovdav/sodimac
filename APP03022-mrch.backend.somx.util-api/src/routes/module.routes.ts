// src/routes/module.routes.ts
import { Router } from 'express';
import { moduleController } from '../controllers/module.controller.js';

const router = Router();

router.get('/', moduleController.getAll);
router.get('/:id', moduleController.getById);
router.post('/', moduleController.create);
router.patch('/:id', moduleController.update);
router.delete('/:id', moduleController.delete);

export default router;

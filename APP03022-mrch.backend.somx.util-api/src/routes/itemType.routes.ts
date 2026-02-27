// src/routes/itemType.routes.ts
import { Router } from 'express';
import { itemTypeController } from '../controllers/itemType.controller.js';

const router = Router();

router.get('/', itemTypeController.getAll);
router.get('/:id', itemTypeController.getById);
router.post('/', itemTypeController.create);
router.patch('/:id', itemTypeController.update);
router.delete('/:id', itemTypeController.delete);

export default router;

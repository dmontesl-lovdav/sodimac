// src/routes/process.routes.ts
import { Router } from 'express';
import { processController } from '../controllers/process.controller.js';

const router = Router();

router.get('/', processController.getAll);
router.get('/:id', processController.getById);
router.post('/', processController.create);
router.patch('/:id', processController.update);
router.delete('/:id', processController.delete);

export default router;

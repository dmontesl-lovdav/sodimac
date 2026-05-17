// src/routes/message.routes.ts
import { Router } from 'express';
import { messageController } from '../controllers/message.controller.js';

const router = Router();

router.get('/', messageController.getAll);
router.get('/code/:code', messageController.getByCode);
router.get('/:id', messageController.getById);
router.post('/', messageController.create);
router.patch('/:id', messageController.update);
router.delete('/:id', messageController.delete);

export default router;

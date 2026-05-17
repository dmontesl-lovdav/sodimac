// src/routes/applicationMsg.routes.ts
import { Router } from 'express';
import { applicationMsgController } from '../controllers/applicationMsg.controller.js';

const router = Router();

router.get('/', applicationMsgController.getAll);
router.get('/:id', applicationMsgController.getById);
router.post('/', applicationMsgController.create);
router.delete('/:id', applicationMsgController.delete);

export default router;

import { Router } from 'express';
import * as ctrl from '@/controllers/accountStatement.controller.js';

const router = Router();
router.get('/', ctrl.list);
router.get('/:uuid/pdf', ctrl.getPdf);
router.patch('/:uuid/confirm-review', ctrl.confirmReview);
router.patch('/:uuid/request-review', ctrl.requestReview);
router.get('/:uuid', ctrl.getById);

export default router;

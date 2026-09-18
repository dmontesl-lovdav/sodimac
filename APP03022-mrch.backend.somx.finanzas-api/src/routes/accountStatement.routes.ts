import { Router } from 'express';
import * as ctrl from '@/controllers/accountStatement.controller.js';
import { requirePermission } from '@/middlewares/permission.middleware.js';

const router = Router();
router.post('/batch', ctrl.batch);
router.get('/', ctrl.list);
router.get('/:uuid/report-data', ctrl.getReportData);
router.get('/:uuid/pdf', ctrl.getPdf);
router.patch('/:uuid/confirm-review', requirePermission('EVT0032'), ctrl.confirmReview);
router.patch('/:uuid/request-review', requirePermission('EVT0033'), ctrl.requestReview);
router.delete('/:uuid', ctrl.softDelete);
router.get('/:uuid', ctrl.getById);

export default router;

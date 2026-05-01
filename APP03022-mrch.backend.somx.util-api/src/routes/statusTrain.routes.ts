import { Router } from 'express';
import * as statusTrainController from '@/controllers/statusTrain.controller.js';

const router = Router();

router.get('/validate', statusTrainController.validateTransition);
router.get('/allowed-destinations', statusTrainController.getAllowedDestinations);
router.post('/', statusTrainController.create);
router.put('/:id', statusTrainController.update);
router.delete('/:id', statusTrainController.deleteOne);
router.get('/:id', statusTrainController.findById);

export default router;


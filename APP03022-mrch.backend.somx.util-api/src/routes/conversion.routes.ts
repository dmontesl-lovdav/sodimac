import { Router } from 'express';
import * as conversionController from '@/controllers/conversion.controller.js';

const router = Router();

router.get('/', conversionController.search);
router.post('/', conversionController.create);
router.delete('/', conversionController.deleteMultiple);
router.get('/:id', conversionController.getById);
router.put('/:id', conversionController.update);
router.patch('/:id/principal', conversionController.setPrincipal);
router.delete('/:id', conversionController.deleteOne);

export default router;


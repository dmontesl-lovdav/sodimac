import type { Request, Response, NextFunction } from 'express';
import multer from 'multer';
import * as layoutService from '@/services/layoutValidation.service.js';

const upload = multer({ storage: multer.memoryStorage(), limits: { fileSize: 20 * 1024 * 1024 } });

export const uploadFile = upload.single('file');

export async function validate(req: Request, res: Response, next: NextFunction) {
    try {
        const file = (req as Request & { file?: Express.Multer.File }).file;
        if (!file || !file.buffer || file.size === 0) {
            return res.status(400).json({ error: 'Archivo vacío o no proporcionado' });
        }

        const tipo = String(req.body.tipoCatalogoSeleccionado ?? '').trim();
        const nombre = String(req.body.nombreCatalogo ?? '').trim();

        if (!tipo || !nombre) {
            return res.status(400).json({ error: 'Parámetros tipoCatalogoSeleccionado y nombreCatalogo son requeridos' });
        }

        const result = await layoutService.validateLayout(file.buffer, tipo, nombre);
        res.json(result);
    } catch (err) {
        next(err);
    }
}

export async function getReport(req: Request, res: Response, next: NextFunction) {
    try {
        const reportId = String(req.params.reportId);
        const content = layoutService.getValidationReport(reportId);
        if (content == null) {
            return res.status(404).json({ error: 'Reporte no encontrado' });
        }
        res.setHeader('Content-Type', 'text/plain');
        res.setHeader('Content-Disposition', 'attachment; filename=reporte_errores.txt');
        res.send(content);
    } catch (err) {
        next(err);
    }
}


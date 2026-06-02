import type { Request, Response, NextFunction } from "express";
import * as migoService from "@/services/migo.service.js";
import type {
    ListMigoDocumentsQueryDto,
    ListMigoReceptionsQueryDto,
    RejectMigoDto,
} from "@/schemas/migo.schema.js";
import type { AuthenticatedRequest } from "@/middlewares/authToken.js";

export async function listDocuments(req: Request, res: Response, next: NextFunction) {
    try {
        const q = res.locals.query as ListMigoDocumentsQueryDto;
        const result = await migoService.listDocuments(q);
        return res.status(result.httpStatus).json(result);
    } catch (err) {
        next(err);
    }
}

export async function getDocumentById(req: Request, res: Response, next: NextFunction) {
    try {
        const { id } = req.params;
        const result = await migoService.getDocumentById(id!);
        return res.status(result.httpStatus).json(result);
    } catch (err) {
        next(err);
    }
}

export async function listReceptions(req: Request, res: Response, next: NextFunction) {
    try {
        const q = res.locals.query as ListMigoReceptionsQueryDto;
        q.migoDocumentId = req.params.id!;
        const authToken = (req as AuthenticatedRequest).authToken ?? '';
        const result = await migoService.listReceptions(q, authToken);
        return res.status(result.httpStatus).json(result);
    } catch (err) {
        next(err);
    }
}

export async function uploadCsv(req: Request, res: Response, next: NextFunction) {
    try {
        const file = req.file;
        if (!file) {
            return res.status(400).json({
                success: false,
                status: 400,
                message: "No se proporcionó un archivo CSV",
            });
        }

        const content = file.buffer.toString('utf-8');
        const result = await migoService.uploadCsv(content, file.originalname);
        return res.status(result.httpStatus).json(result);
    } catch (err) {
        next(err);
    }
}

export async function authorizeDocument(req: Request, res: Response, next: NextFunction) {
    try {
        const { id } = req.params;
        const result = await migoService.authorizeDocument(id!);
        return res.status(result.httpStatus).json(result);
    } catch (err) {
        next(err);
    }
}

export async function rejectDocument(req: Request, res: Response, next: NextFunction) {
    try {
        const body = req.body as RejectMigoDto;
        const result = await migoService.rejectDocument(body);
        return res.status(result.httpStatus).json(result);
    } catch (err) {
        next(err);
    }
}

export async function exportReceptionsCsv(req: Request, res: Response, next: NextFunction) {
    try {
        const { id } = req.params;
        const csv = await migoService.exportReceptionsCsv(id!);

        const now = new Date();
        const pad = (n: number) => String(n).padStart(2, '0');
        const yyyymmdd = `${now.getFullYear()}${pad(now.getMonth() + 1)}${pad(now.getDate())}`;
        const hhmmss = `${pad(now.getHours())}.${pad(now.getMinutes())}.${pad(now.getSeconds())}`;
        const fileName = `recepcion_archivo_${id}_${yyyymmdd}_${hhmmss}.csv`;

        res.setHeader('Content-Type', 'text/csv; charset=utf-8');
        res.setHeader('Content-Disposition', `attachment; filename="${fileName}"`);
        return res.send(csv);
    } catch (err) {
        next(err);
    }
}

// src/middlewares/errorHandler.ts
import type { Request, Response, NextFunction } from 'express';
import { ZodError } from 'zod';
import { QueryFailedError } from 'typeorm';
import { UtilsException } from '../exceptions/UtilsException.js';

export interface HttpError extends Error {
    status?: number;
    code?: string;
}

/* --------------- helpers (type guards) --------------- */
function hasStatus(e: unknown): e is { status: number; message?: string } {
    return (
        typeof e === 'object' &&
        e !== null &&
        'status' in e &&
        typeof (e as Record<string, unknown>).status === 'number'
    );
}

function isJsonParseError(e: unknown): e is { type: 'entity.parse.failed' } {
    return (
        typeof e === 'object' &&
        e !== null &&
        (e as Record<string, unknown>).type === 'entity.parse.failed'
    );
}

function pickDriverDetail(e: unknown): string | undefined {
    if (typeof e !== 'object' || e === null) return undefined;
    const r = e as Record<string, unknown>;
    const de = r.driverError;
    if (typeof de === 'object' && de !== null) {
        const d = de as Record<string, unknown>;
        if (typeof d.detail === 'string') return d.detail;
    }
    return undefined;
}

function pickCode(e: unknown): string | undefined {
    if (typeof e !== 'object' || e === null) return undefined;
    const r = e as Record<string, unknown>;
    return typeof r.code === 'string' ? r.code : undefined;
}

/* ----------------- middleware ----------------- */
export function errorHandler(err: unknown, req: Request, res: Response, _next: NextFunction) {
    void _next;

    console.error('Error capturado:', err);

    let status = 500;
    let message = 'Internal server error';
    let code: string | undefined;
    let details: unknown;

    // 0) UtilsException (excepcion personalizada del sistema)
    if (err instanceof UtilsException) {
        status = err.httpStatus;
        message = err.message;
        code = err.code;
        if (err.additionalInfo) {
            details = { additionalInfo: err.additionalInfo };
        }
    }
    // 1) HttpError propio (throw { status, message })
    else if (hasStatus(err)) {
        status = err.status;
        message = err.message ?? message;
    }
    // 2) Zod (usa `issues`)
    else if (err instanceof ZodError) {
        status = 400;
        message = 'Validation failed';
        details = err.issues.map(i => ({
            path: i.path.map(String).join('.'),
            code: i.code,
            message: i.message,
        }));
    }
    // 3) SQL / TypeORM
    else if (err instanceof QueryFailedError) {
        status = 400;
        message = 'Database query failed';
        details = {
            code: pickCode(err),
            driver: pickDriverDetail(err) ?? err.message,
        };
    }
    // 4) JSON malformado (body-parser)
    else if (isJsonParseError(err)) {
        status = 400;
        message = 'Invalid JSON in request body';
    }
    // 5) Red/conexion
    else if (pickCode(err) === 'ECONNREFUSED' || pickCode(err) === 'ETIMEDOUT') {
        status = 503;
        message = 'Service temporarily unavailable';
    }
    // 6) Fallback
    else if (typeof err === 'object' && err !== null && 'message' in err) {
        const r = err as Record<string, unknown>;
        if (typeof r.message === 'string') message = r.message;
    }

    if (process.env.NODE_ENV === 'development') {
        if (!details && typeof err === 'object' && err !== null && 'stack' in err) {
            const r = err as Record<string, unknown>;
            details = typeof r.stack === 'string' ? r.stack : details;
        }
    }

    res.status(status).json({
        success: false,
        status,
        code,
        message,
        details,
    });
}

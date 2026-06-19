// src/middlewares/errorHandler.ts
import type { Request, Response, NextFunction } from 'express';
import { ZodError } from 'zod';
import { QueryFailedError } from 'typeorm';
import { logActivity } from '@/middlewares/logger.js';


export interface HttpError extends Error {
    status?: number;
    code?: string;
}

/* ─────────────── helpers (type guards) ─────────────── */
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

function safeStringify(v: unknown) {
    try {
        return JSON.stringify(v);
    } catch {
        return String(v);
    }
}


type ErrorResult = {
    status: number;
    message: string;
    details?: unknown;
};

function handleHttpError(err: unknown, base: ErrorResult): ErrorResult | null {
    if (hasStatus(err)) {
        return {
            status: err.status,
            message: err.message ?? base.message,
        };
    }
    return null;
}

function handleZodError(err: unknown): ErrorResult | null {
    if (err instanceof ZodError) {
        return {
            status: 400,
            message: 'Validation failed',
            details: err.issues.map(i => ({
                path: i.path.map(String).join('.'),
                code: i.code,
                message: i.message,
            })),
        };
    }
    return null;
}

function handleDbError(err: unknown): ErrorResult | null {
    if (err instanceof QueryFailedError) {
        return {
            status: 400,
            message: 'Database query failed',
            details: {
                code: pickCode(err),
                driver: pickDriverDetail(err) ?? err.message,
            },
        };
    }
    return null;
}

function handleJsonError(err: unknown): ErrorResult | null {
    if (isJsonParseError(err)) {
        return {
            status: 400,
            message: 'Invalid JSON in request body',
        };
    }
    return null;
}

function handleNetworkError(err: unknown): ErrorResult | null {
    const code = pickCode(err);
    if (code === 'ECONNREFUSED' || code === 'ETIMEDOUT') {
        return {
            status: 503,
            message: 'Service temporarily unavailable',
        };
    }
    return null;
}

function handleFallback(err: unknown, base: ErrorResult): ErrorResult {
    if (typeof err === 'object' && err !== null && 'message' in err) {
        const msg = (err as any).message;
        if (typeof msg === 'string') {
            return { ...base, message: msg };
        }
    }
    return base;
}

function resolveError(err: unknown): ErrorResult {
    const base: ErrorResult = {
        status: 500,
        message: 'Internal server error',
    };

    return (
        handleHttpError(err, base) ??
        handleZodError(err) ??
        handleDbError(err) ??
        handleJsonError(err) ??
        handleNetworkError(err) ??
        handleFallback(err, base)
    );
}

function attachStackIfDev(err: unknown, details: unknown): unknown {
    if (process.env.NODE_ENV !== 'development') return details;

    if (
        !details &&
        typeof err === 'object' &&
        err !== null &&
        'stack' in err
    ) {
        return typeof (err as any).stack === 'string'
            ? (err as any).stack
            : details;
    }

    return details;
}

/* ───────────────── middleware ───────────────── */
export function errorHandler(err: unknown, req: Request, res: Response, next: NextFunction) {

    if (res.headersSent) {
        return next(err);
    }
    console.error('💥 Error capturado:', err);

    const { status, message, details } = resolveError(err);
    const finalDetails = attachStackIfDev(err, details);

    
    void logActivity(
        true,
        'ERROR_HANDLER',
        message,
        {
            method: req.method,
            url: req.originalUrl,
            ip: req.ip,
            query: req.query,
            body: req.body,
            response_status: status,
            error: safeStringify(err),
            details: finalDetails,
        },
        0,
        {
            tipoEvento: 'ERROR',
            codigoError: String(status),
            idMensaje:
                err instanceof ZodError
                    ? 'ValidationError'
                    : validateQueryFailedError(err),
            paso: `${req.method} ${req.originalUrl}`,
            log:
                typeof err === 'object' && err !== null && 'stack' in err
                    ? String((err as any).stack)
                    : safeStringify(err),
        }
    );


    res.status(status).json({
        success: false,
        status,
        message,
        details: finalDetails,
    });

}


function validateQueryFailedError(err: unknown): string | null {
    return err instanceof QueryFailedError
        ? 'DatabaseQueryFailed'
        : validateJsonParser(err);
}

function validateJsonParser(err: unknown): string | null {
    return isJsonParseError(err)
        ? 'InvalidJson'
        : pickCode(err) ?? 'UnhandledError';
}

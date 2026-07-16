import type { Request, Response, NextFunction } from 'express';
import { ZodError } from 'zod';
import { QueryFailedError } from 'typeorm';
import { UtilsException } from '../exceptions/UtilsException.js';
import { GenericException } from '../exceptions/GenericException.js';
import { ConflictException } from '../exceptions/ConflictException.js';

export interface HttpError extends Error {
    status?: number;
    code?: string;
}

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

interface ErrorMapping {
    status: number;
    message: string;
    code?: string;
    details?: unknown;
}

const DEFAULT_MAPPING: ErrorMapping = {
    status: 500,
    message: 'Internal server error',
};

function mapUtilsException(err: UtilsException): ErrorMapping {
    const mapping: ErrorMapping = {
        status: err.httpStatus,
        message: err.message,
        code: err.code,
    };
    if (err.additionalInfo) {
        mapping.details = { additionalInfo: err.additionalInfo };
    }
    return mapping;
}

function mapZodError(err: ZodError): ErrorMapping {
    return {
        status: 400,
        message: 'Validation failed',
        details: err.issues.map((i) => ({
            path: i.path.map(String).join('.'),
            code: i.code,
            message: i.message,
        })),
    };
}

function mapQueryFailed(err: QueryFailedError): ErrorMapping {
    return {
        status: 400,
        message: 'Database query failed',
        details: {
            code: pickCode(err),
            driver: pickDriverDetail(err) ?? err.message,
        },
    };
}

function mapPlainMessage(err: unknown): ErrorMapping {
    if (typeof err !== 'object' || err === null || !('message' in err)) return DEFAULT_MAPPING;
    const r = err as Record<string, unknown>;
    if (typeof r.message !== 'string') return DEFAULT_MAPPING;
    return { ...DEFAULT_MAPPING, message: r.message };
}

function classifyError(err: unknown): ErrorMapping {
    if (err instanceof UtilsException) return mapUtilsException(err);
    if (err instanceof GenericException) return { status: err.code, message: err.message };
    if (err instanceof ConflictException) return { status: 409, message: err.message, code: err.errorType };
    if (hasStatus(err)) {
        const mapping: ErrorMapping = {
            status: err.status,
            message: err.message ?? DEFAULT_MAPPING.message,
        };
        const businessCode = pickCode(err);
        if (businessCode !== undefined) mapping.code = businessCode;
        return mapping;
    }
    if (err instanceof ZodError) return mapZodError(err);
    if (err instanceof QueryFailedError) return mapQueryFailed(err);
    if (isJsonParseError(err)) return { status: 400, message: 'Invalid JSON in request body' };
    const code = pickCode(err);
    if (code === 'ECONNREFUSED' || code === 'ETIMEDOUT') {
        return { status: 503, message: 'Service temporarily unavailable' };
    }
    return mapPlainMessage(err);
}

function pickStackDetail(err: unknown, currentDetails: unknown): unknown {
    if (currentDetails) return currentDetails;
    if (typeof err !== 'object' || err === null || !('stack' in err)) return currentDetails;
    const r = err as Record<string, unknown>;
    return typeof r.stack === 'string' ? r.stack : currentDetails;
}

export function errorHandler(err: unknown, req: Request, res: Response, _next: NextFunction) {
    console.error('Error capturado:', err);

    const mapping = classifyError(err);
    const details =
        process.env.NODE_ENV === 'development' ? pickStackDetail(err, mapping.details) : mapping.details;

    res.status(mapping.status).json({
        success: false,
        status: mapping.status,
        code: mapping.code,
        message: mapping.message,
        details,
    });
}


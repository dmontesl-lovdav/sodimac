import type { Request, Response, NextFunction } from "express";
import type { ZodSchema, ZodError } from "zod";
import { ResponseHandler } from '@/response/ResponseHandler.js';
import { StatusCodes } from 'http-status-codes';

type AnyObj = Record<string, unknown>;

function sendZodError(_req: Request, res: Response, err: ZodError, messageValidation = "") {
    if (messageValidation === "") {
        messageValidation = "ValidationError";
    }
    return res.status(400).json(ResponseHandler.responseBuilder(messageValidation, null, -1, StatusCodes.BAD_REQUEST, false, flattenZodErrors(err)));
}

export function validateQuery<T extends AnyObj>(schema: ZodSchema<T>) {
    return (req: Request, res: Response, next: NextFunction) => {
        const parsed = schema.safeParse(req.query);
        if (!parsed.success) return sendZodError(req, res, parsed.error);

        Object.assign(req.query as AnyObj, parsed.data as AnyObj);
        res.locals.query = parsed.data;
        next();
    };
}

export function validateParams<T extends AnyObj>(schema: ZodSchema<T>) {
    return (req: Request, res: Response, next: NextFunction) => {
        const parsed = schema.safeParse(req.params);
        if (!parsed.success) return sendZodError(req, res, parsed.error);

        Object.assign(req.params as AnyObj, parsed.data as AnyObj);
        res.locals.params = parsed.data;
        next();
    };
}

export function validateBody<T extends AnyObj>(schema: ZodSchema<T>) {
    return (req: Request, res: Response, next: NextFunction) => {
        const parsed = schema.safeParse(req.body);
        if (!parsed.success) return sendZodError(req, res, parsed.error);
        req.body = parsed.data as unknown as Request["body"];
        res.locals.body = parsed.data;
        next();
    };
}

export function flattenZodErrors(error: ZodError) {
    return error.issues.map(err => ({
        path: err.path.join('.'),
        message: err.message,
    }));
}

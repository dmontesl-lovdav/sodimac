// src/middlewares/validate.ts
import type { Request, Response, NextFunction } from "express";
import { ZodError, ZodSchema } from "zod";
import { ResponseHandler } from '@/response/ResponseHandler.js';
import { StatusCodes } from 'http-status-codes';
import {
  CreateShippingGuideSchema,
} from "@/schemas/shippingGuide.schema.js";
import {
  BaseArrayFilesSchemaParent,
  BaseSchemaParent,
} from "@/schemas/base.shema.js";
import { z } from "zod/v4";
import { logActivity } from '@/middlewares/logger.js';

type AnyObj = Record<string, unknown>;

async function sendZodError(
  req: Request,
  res: Response,
  err: ZodError,
  messageValidation = ""
) {
  if (messageValidation === "") {
    messageValidation = "ValidationError";
  }

  const flattened = flattenZodErrors(err);

  // CONSOLE LOG DETALLADO
  console.error("========================================");
  console.error("VALIDATION ERROR");
  console.error("Route:", req.method, req.originalUrl);
  console.error("Query:", req.query);
  console.error("Params:", req.params);
  console.error("Body:", req.body);
  console.error("Zod Issues:", flattened);
  console.error("========================================");

  await logActivity(true, 'ValidationError', flattened, req.body);

  return res.status(400).json(
    ResponseHandler.responseBuilder(
      messageValidation,
      null,
      -1,
      StatusCodes.BAD_REQUEST,
      false,
      flattened
    )
  );
}

export function validateQuery<T extends AnyObj>(schema: ZodSchema<T>) {
  return (req: Request, res: Response, next: NextFunction) => {
    const parsed = schema.safeParse(req.query);

    if (!parsed.success) {
      return sendZodError(req, res, parsed.error);
    }

    Object.assign(req.query as AnyObj, parsed.data as AnyObj);
    res.locals.query = parsed.data;

    next();
  };
}

export function validateParams<T extends AnyObj>(schema: ZodSchema<T>) {
  return (req: Request, res: Response, next: NextFunction) => {
    const parsed = schema.safeParse(req.params);

    if (!parsed.success) {
      return sendZodError(req, res, parsed.error);
    }

    Object.assign(req.params as AnyObj, parsed.data as AnyObj);
    res.locals.params = parsed.data;

    next();
  };
}

export function validateBody<T extends AnyObj>(schema: ZodSchema<T>) {
  return (req: Request, res: Response, next: NextFunction) => {
    const parsed = schema.safeParse(req.body);

    if (!parsed.success) {
      return sendZodError(req, res, parsed.error);
    }

    req.body = parsed.data as unknown as Request["body"];
    res.locals.body = parsed.data;

    next();
  };
}

export function validateFormData<T extends AnyObj>(
  schema: ZodSchema<T>,
  schemaParent: typeof BaseSchemaParent,
  message = ""
) {
  return (req: Request, res: Response, next: NextFunction) => {
    const parsed = schemaParent.safeParse(req.body);

    if (!parsed.success) {
      return sendZodError(req, res, parsed.error, message);
    }

    const parsedData = JSON.parse(parsed.data.content);
    const parsed2 = schema.safeParse(parsedData);

    if (parsed2 != null && !parsed2.success) {
      return sendZodError(req, res, parsed2.error, message);
    }

    req.body = parsed.data as unknown as Request["body"];
    res.locals.body = parsed.data;

    next();
  };
}

export function validateArrayFilesAndCPObj<T extends AnyObj>(
  schema: ZodSchema<T>,
  schemaParent: typeof BaseArrayFilesSchemaParent,
  message = ""
) {
  return (req: Request, res: Response, next: NextFunction) => {
    const parsed = schemaParent.safeParse({ ...req.body, files: req.files });

    if (!parsed.success) {
      return sendZodError(req, res, parsed.error, message);
    }

    const parsedData = JSON.parse(parsed.data.content);
    const parsed2 = schema.safeParse(parsedData);

    if (parsed2 != null && !parsed2.success) {
      return sendZodError(req, res, parsed2.error, message);
    }

    req.body = parsed.data as unknown as Request["body"];
    res.locals.body = parsed.data;

    next();
  };
}

function validateJsonString(
  schema: typeof CreateShippingGuideSchema,
  dataString: string
) {
  try {
    const parsedData = JSON.parse(dataString);
    return schema.safeParse(parsedData);
  } catch (error) {
    console.error("JSON/Zod parsing error:", error);
    return null;
  }
}

export function flattenZodErrors(error: ZodError) {
  return error.issues.map(err => ({
    path: err.path.join('.'),
    message: err.message,
  }));
}

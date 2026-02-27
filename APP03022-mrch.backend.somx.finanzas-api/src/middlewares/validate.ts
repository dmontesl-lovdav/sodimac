// src/middlewares/validate.ts
import type { Request, Response, NextFunction } from "express";
import { ZodSafeParseError, ZodIssue, ZodError, ZodSafeParseResult, ZodSchema  } from "zod";
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
import { logActivity  } from '@/middlewares/logger.js';



type AnyObj = Record<string, unknown>;

async function sendZodError(req: Request, res: Response, err: ZodError, messageValidation = "") {
    if(messageValidation === ""){
      messageValidation = "ValidationError";
    }
    await logActivity(true, 'ValidationError', flattenZodErrors(err) , req.body);
    return res.status(400).json(ResponseHandler.responseBuilder(messageValidation,null,-1, StatusCodes.BAD_REQUEST, false, flattenZodErrors(err)));
}

export function validateQuery<T extends AnyObj>(schema: ZodSchema<T>) {
    return (req: Request, res: Response, next: NextFunction) => {
        const parsed = schema.safeParse(req.query);
        if (!parsed.success) return sendZodError(req,res, parsed.error);

        // ❗ No reasignar req.query (read-only). Mezcla los valores validados.
        Object.assign(req.query as AnyObj, parsed.data as AnyObj);
        // opcional: también disponible en locals
        res.locals.query = parsed.data;
        next();
    };
}

export function validateParams<T extends AnyObj>(schema: ZodSchema<T>) {
      
    return (req: Request, res: Response, next: NextFunction) => {
        const parsed = schema.safeParse(req.params);
        if (!parsed.success) return sendZodError(req,res, parsed.error);

        // ❗ No reasignar req.params. Mezcla.
        Object.assign(req.params as AnyObj, parsed.data as AnyObj);
        res.locals.params = parsed.data;
        next();
    };
}

export function validateBody<T extends AnyObj>(schema: ZodSchema<T>) {
    return (req: Request, res: Response, next: NextFunction) => {
        const parsed = schema.safeParse(req.body);
        if (!parsed.success) return sendZodError(req,res, parsed.error);
        req.body = parsed.data as unknown as Request["body"];

        res.locals.body = parsed.data;
        next();
    };
}

export function validateFormData<T extends AnyObj>(schema: ZodSchema<T>,schemaParent: typeof BaseSchemaParent, message = "") {
return (req: Request, res: Response, next: NextFunction) => {
        const parsed = schemaParent.safeParse(req.body);
        if (!parsed.success) { 
          return sendZodError(req,res, parsed.error, message);
        }
        const parsedData = JSON.parse(parsed.data.content);
        //Validate the parsed object against the Zod schema
        const parsed2 = schema.safeParse(parsedData);
        if (parsed2 != null && !parsed2.success) {
          return sendZodError(req,res, parsed2.error, message);
        }
        req.body = parsed.data as unknown as Request["body"];
        res.locals.body = parsed.data;
        next();
    };
}

export function validateArrayFilesAndCPObj<T extends AnyObj>(schema: ZodSchema<T>,schemaParent: typeof BaseArrayFilesSchemaParent, message = "") {
    return (req: Request, res: Response, next: NextFunction) => {
        const parsed = schemaParent.safeParse({...req.body, files:req.files});
        if (!parsed.success) {
          return sendZodError(req,res, parsed.error, message);
        }
        const parsedData = JSON.parse(parsed.data.content);
        //Validate the parsed object against the Zod schema
        const parsed2 = schema.safeParse(parsedData);
        if (parsed2 != null && !parsed2.success) {
          return sendZodError(req,res, parsed2.error, message);
        }
        req.body = parsed.data as unknown as Request["body"];
        res.locals.body = parsed.data;
        next();
    };
}


function validateJsonString(schema: typeof CreateShippingGuideSchema, dataString: string) {
  try {
    // Attempt to parse the JSON string
    const parsedData = JSON.parse(dataString);

    // Validate the parsed object against the Zod schema
    const parsed = schema.safeParse(parsedData);
    return parsed;
  } catch (error) {
    if (error instanceof z.ZodError) {
      console.error("Zod validation error:", error.issues);
    } else if (error instanceof SyntaxError) {
      console.error("JSON parsing error:", error.message);
    } else {
      console.error("An unexpected error occurred:", error);
    }
    return null;
  }
}


export function flattenZodErrors(error: ZodError) {
  return error.issues.map(err => ({
    path: err.path.join('.'),
    message: err.message,
  }));
}




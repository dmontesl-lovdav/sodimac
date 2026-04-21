import type { Request, Response, NextFunction } from "express";
import { StatusCodes } from "http-status-codes";
import { ResponseHandler } from "@/response/ResponseHandler.js";

export function validateInternalServiceAuth(req: Request, res: Response, next: NextFunction) {
    const expectedToken = process.env.INTERNAL_SERVICE_TOKEN;
    const receivedToken = req.headers["x-internal-token"];

    if (!expectedToken) {
        return next();
    }

    if (!receivedToken || receivedToken !== expectedToken) {
        return res.status(StatusCodes.UNAUTHORIZED).json(
            ResponseHandler.responseBuilder(
                "Unauthorized",
                null,
                -1,
                StatusCodes.UNAUTHORIZED,
                false,
                "Invalid internal service token"
            )
        );
    }

    next();
}
import type { Request, Response, NextFunction } from "express";
import { StatusCodes } from "http-status-codes";
import { ResponseHandler } from "@/response/ResponseHandler.js";

export interface AuthenticatedRequest extends Request {
    authToken?: string;
}

export function attachAuthToken(
    req: AuthenticatedRequest,
    res: Response,
    next: NextFunction
) {
    const securityEnabled = String(process.env.SECURITY_ENABLED).toLowerCase() === "true";
    const authorizationHeader = req.headers.authorization;

    if (!securityEnabled) {
        if (authorizationHeader?.startsWith("Bearer ")) {
            const token = authorizationHeader.slice(7).trim();
            if (token) {
                req.authToken = token;
            }
        }
        return next();
    }

    if (!authorizationHeader) {
        return res.status(StatusCodes.UNAUTHORIZED).json(
            ResponseHandler.responseBuilder(
                "Unauthorized",
                null,
                -1,
                StatusCodes.UNAUTHORIZED,
                false,
                "Authorization header is required"
            )
        );
    }

    if (!authorizationHeader.startsWith("Bearer ")) {
        return res.status(StatusCodes.UNAUTHORIZED).json(
            ResponseHandler.responseBuilder(
                "Unauthorized",
                null,
                -1,
                StatusCodes.UNAUTHORIZED,
                false,
                "Invalid authorization format"
            )
        );
    }

    const token = authorizationHeader.slice(7).trim();

    if (!token) {
        return res.status(StatusCodes.UNAUTHORIZED).json(
            ResponseHandler.responseBuilder(
                "Unauthorized",
                null,
                -1,
                StatusCodes.UNAUTHORIZED,
                false,
                "Token not provided"
            )
        );
    }

    req.authToken = token;

    return next();
}
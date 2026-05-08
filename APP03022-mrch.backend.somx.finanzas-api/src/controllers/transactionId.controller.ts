import type { Request, Response, NextFunction } from "express";
import { StatusCodes } from "http-status-codes";
import { ResponseHandler } from "@/response/ResponseHandler.js";
import * as svc from "@/services/transactionId.service.js";
import { getTraceId } from "@/middlewares/logger.js";
import {
    CreateTransactionIdSchema,
    TransactionIdFolioParamSchema,
    type CreateTransactionIdDto,
    type TransactionIdFolioParamDto,
} from "@/schemas/transactionId.schema.js";
import { AuthenticatedRequest } from "@/middlewares/authToken.js";

export async function create(req: AuthenticatedRequest, res: Response, next: NextFunction) {
    try {
        const dto: CreateTransactionIdDto = CreateTransactionIdSchema.parse(req.body);

        const idUsuario =
            (req.headers["x-user-id"] as string | undefined) ||
            dto.idUsuario ||
            "system";

        const origen =
            (req.headers["x-service-name"] as string | undefined) ||
            dto.origen ||
            "finanzas-api";

        const created = await svc.create(dto, req.authToken ?? '', { idUsuario, origen });

        res.status(StatusCodes.CREATED).json({
            ...ResponseHandler.responseBuilder(
                "Transaction ID generated successfully",
                created,
                0,
                StatusCodes.CREATED,
                true,
                ""
            ),
            trace_id: getTraceId(),
        });
    } catch (e) {
        next(e);
    }
}

export async function detailByFolioVisible(req: Request, res: Response, next: NextFunction) {
    try {
        const params: TransactionIdFolioParamDto = TransactionIdFolioParamSchema.parse(req.params);
        const result = await svc.detailByFolioVisible(params.folioVisible);

        res.status(StatusCodes.OK).json({
            ...ResponseHandler.responseBuilder(
                "Transaction ID detail",
                result,
                0,
                StatusCodes.OK,
                true,
                ""
            ),
            trace_id: getTraceId(),
        });
    } catch (e) {
        next(e);
    }
}
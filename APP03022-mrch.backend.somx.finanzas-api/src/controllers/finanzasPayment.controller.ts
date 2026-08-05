import type { Request, Response, NextFunction } from "express";
import { StatusCodes } from "http-status-codes";
import * as svc from "@/services/finanzasPayment.service.js";
import { ResponseHandler } from "@/response/ResponseHandler.js";
import {
    CreateFinanzasPaymentSchema,
    UpdateFinanzasPaymentSchema,
    ListFinanzasPaymentsQuerySchema,
    CreateFinanzasPaymentHeaderWithDetailsSchema,
    PaymentHeaderUuidParamSchema,
    ListFinanzasPaymentDetailsByHeaderQuerySchema,
    type CreateFinanzasPaymentDto,
    type UpdateFinanzasPaymentDto,
    type ListFinanzasPaymentQuery,
    type CreateFinanzasPaymentHeaderWithDetailsDto,
    type PaymentHeaderUuidParamDto,
    type ListFinanzasPaymentDetailsByHeaderQueryDto,
} from "@/schemas/finanzasPayment.schema.js";

// GET /finanzas-payment
export async function list(
    req: Request,
    res: Response,
    _next: NextFunction
) {
    const start = Date.now();

    try {
        console.log("[finanzas-payment][controller.list] START", {
            method: req.method,
            originalUrl: req.originalUrl,
            query: req.query,
            headers: {
                authorization: req.headers.authorization
                    ? "[PRESENT]"
                    : "[MISSING]",
                "x-country": req.headers["x-country"],
                "x-commerce": req.headers["x-commerce"],
            },
        });

        const q: ListFinanzasPaymentQuery =
            ListFinanzasPaymentsQuerySchema.parse(req.query);

        console.log(
            "[finanzas-payment][controller.list] PARSED_QUERY",
            q
        );

        const response = await svc.list(q);

        console.log("[finanzas-payment][controller.list] END", {
            elapsedMs: Date.now() - start,
            success: true,
        });

        return res.status(StatusCodes.OK).json(response);
    } catch (e) {
        console.error("[finanzas-payment][controller.list] ERROR", {
            elapsedMs: Date.now() - start,
            error: e,
        });

        return res.status(StatusCodes.BAD_REQUEST).json(
            ResponseHandler.responseBuilder(
                "ERROR: " + e,
                null,
                -1,
                StatusCodes.BAD_REQUEST,
                false,
                ""
            )
        );
    }
}

// POST /finanzas-payment
export async function create(
    req: Request,
    res: Response,
    _next: NextFunction
) {
    const start = Date.now();

    try {
        console.log("[finanzas-payment][controller.create] START", {
            method: req.method,
            originalUrl: req.originalUrl,
        });

        const dto: CreateFinanzasPaymentDto =
            CreateFinanzasPaymentSchema.parse(req.body);

        const created = await svc.create(dto);

        console.log("[finanzas-payment][controller.create] END", {
            elapsedMs: Date.now() - start,
            success: true,
        });

        return res.status(StatusCodes.CREATED).json(created);
    } catch (e) {
        console.error("[finanzas-payment][controller.create] ERROR", {
            elapsedMs: Date.now() - start,
            error: e,
        });

        return res.status(StatusCodes.BAD_REQUEST).json(
            ResponseHandler.responseBuilder(
                "ERROR: " + e,
                null,
                -1,
                StatusCodes.BAD_REQUEST,
                false,
                ""
            )
        );
    }
}

// POST /finanzas-payment/header-with-details
export async function createHeaderWithDetails(
    req: Request,
    res: Response,
    _next: NextFunction
) {
    const start = Date.now();

    try {
        console.log(
            "[finanzas-payment][controller.createHeaderWithDetails] START",
            {
                method: req.method,
                originalUrl: req.originalUrl,
            }
        );

        const dto: CreateFinanzasPaymentHeaderWithDetailsDto =
            CreateFinanzasPaymentHeaderWithDetailsSchema.parse(
                req.body
            );

        const created =
            await svc.createHeaderWithDetails(dto);

        console.log(
            "[finanzas-payment][controller.createHeaderWithDetails] END",
            {
                elapsedMs: Date.now() - start,
                success: true,
            }
        );

        return res.status(StatusCodes.CREATED).json(created);
    } catch (e) {
        console.error(
            "[finanzas-payment][controller.createHeaderWithDetails] ERROR",
            {
                elapsedMs: Date.now() - start,
                error: e,
            }
        );

        return res.status(StatusCodes.BAD_REQUEST).json(
            ResponseHandler.responseBuilder(
                "ERROR: " + e,
                null,
                -1,
                StatusCodes.BAD_REQUEST,
                false,
                ""
            )
        );
    }
}

// PATCH /finanzas-payment
export async function update(
    req: Request,
    res: Response,
    _next: NextFunction
) {
    const start = Date.now();

    try {
        console.log("[finanzas-payment][controller.update] START", {
            method: req.method,
            originalUrl: req.originalUrl,
            body: req.body,
        });

        const dto: UpdateFinanzasPaymentDto =
            UpdateFinanzasPaymentSchema.parse(req.body);

        const {
            finanzasPaymentUuid,
            ...patch
        } = dto;

        const updated = await svc.update(
            finanzasPaymentUuid,
            patch
        );

        console.log("[finanzas-payment][controller.update] END", {
            elapsedMs: Date.now() - start,
            success: true,
            finanzasPaymentUuid,
        });

        return res
            .status(StatusCodes.OK)
            .json(updated);
    } catch (e) {
        console.error("[finanzas-payment][controller.update] ERROR", {
            elapsedMs: Date.now() - start,
            error: e,
        });

        return res.status(StatusCodes.BAD_REQUEST).json(
            ResponseHandler.responseBuilder(
                "ERROR: " + e,
                null,
                -1,
                StatusCodes.BAD_REQUEST,
                false,
                ""
            )
        );
    }
}

// GET /finanzas-payment/header-with-details/:paymentHeaderUuid
export async function getHeaderWithDetails(
    req: Request,
    res: Response,
    _next: NextFunction
) {
    const start = Date.now();

    try {
        console.log(
            "[finanzas-payment][controller.getHeaderWithDetails] START",
            {
                method: req.method,
                originalUrl: req.originalUrl,
                params: req.params,
                query: req.query,
            }
        );

        const params: PaymentHeaderUuidParamDto =
            PaymentHeaderUuidParamSchema.parse(req.params);

        let queryDto:
            | ListFinanzasPaymentDetailsByHeaderQueryDto
            | undefined;

        if (
            req.query.pageNumber !== undefined ||
            req.query.pageSize !== undefined
        ) {
            queryDto =
                ListFinanzasPaymentDetailsByHeaderQuerySchema.parse(
                    req.query
                );
        }

        const response =
            await svc.getHeaderWithDetails(
                params.paymentHeaderUuid,
                queryDto
            );

        console.log(
            "[finanzas-payment][controller.getHeaderWithDetails] END",
            {
                elapsedMs: Date.now() - start,
                success: true,
            }
        );

        return res.status(StatusCodes.OK).json(response);
    } catch (e) {
        console.error(
            "[finanzas-payment][controller.getHeaderWithDetails] ERROR",
            {
                elapsedMs: Date.now() - start,
                error: e,
            }
        );

        return res.status(StatusCodes.BAD_REQUEST).json(
            ResponseHandler.responseBuilder(
                "ERROR: " + e,
                null,
                -1,
                StatusCodes.BAD_REQUEST,
                false,
                ""
            )
        );
    }
}
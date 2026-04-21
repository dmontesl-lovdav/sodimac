import * as r from "@/repositories/FinanzasPayment.repo.js";
import * as headerRepo from "@/repositories/FinanzasPaymentHeader.repo.js";
import { ResponsePageableDTO } from "@/response/ResponseHandler.dto.js";
import { ResponseHandler } from "@/response/ResponseHandler.js";
import { StatusCodes } from "http-status-codes";
import { z } from "zod/v4";
import type {
    ListFinanzasPaymentQuery,
    CreateFinanzasPaymentDto,
    UpdateFinanzasPaymentDto,
    CreateFinanzasPaymentHeaderWithDetailsDto,
    ListFinanzasPaymentDetailsByHeaderQueryDto,
} from "@/schemas/finanzasPayment.schema.js";
import { FinanzasPaymentHeader } from "@/entities/FinanzasPaymentHeader.entity.js";
import { FinanzasPayment } from "@/entities/FinanzasPayment.entities.js";
import { Between, MoreThanOrEqual, FindOptionsWhere } from "typeorm";
import { datasource } from "@/config/typeorm-datasource.js";
import { randomUUID } from "crypto";

export async function list(q: ListFinanzasPaymentQuery) {
    const start = Date.now();
    console.log("[finanzas-payment][service.list] START", q);

    const filter: FindOptionsWhere<FinanzasPayment> = {};

    if (q.documentNumber !== undefined) filter.documentNumber = q.documentNumber;
    if (q.vendorNumber !== undefined) filter.vendorNumber = q.vendorNumber;
    if (q.finanzasPaymentUuid !== undefined) filter.finanzasPaymentUuid = q.finanzasPaymentUuid as string;
    if (q.sapDocument !== undefined) filter.sapDocument = q.sapDocument;
    if (q.paymentDate) filter.paymentDate = MoreThanOrEqual(q.paymentDate);

    const createdAtEndString = q.createdAtEnd;
    const parsedDateEnd = z.coerce.date().parse(createdAtEndString);
    parsedDateEnd.setDate(parsedDateEnd.getDate() + 1);

    if (q.createdAtInitial && parsedDateEnd) {
        filter.createdAt = Between(q.createdAtInitial, parsedDateEnd);
    }

    console.log("[finanzas-payment][service.list] FILTER", filter);

    const repoStart = Date.now();
    const [result, total, numberOfElements] = await r.findAllPaginated(filter, q.pageSize, q.pageNumber);
    console.log("[finanzas-payment][service.list] REPO_DONE", {
        repoElapsedMs: Date.now() - repoStart,
        rows: result.length,
        total,
        numberOfElements,
    });

    const totalItems = Number(total?.valueOf() == null ? 0 : Number(total?.valueOf()));
    let totalPages = totalItems / q.pageSize;

    if (totalPages - Math.trunc(totalPages) > 0) {
        totalPages = Math.trunc(totalPages) + 1;
    } else {
        totalPages = Math.trunc(totalPages);
    }

    const responsePageableDTO: ResponsePageableDTO = {
        content: result,
        totalElements: totalItems,
        numberOfElements: numberOfElements?.valueOf() == null ? 0 : Number(numberOfElements?.valueOf()),
        totalPages,
        pageNumber: q.pageNumber,
        pageSize: q.pageSize,
    };

    console.log("[finanzas-payment][service.list] END", {
        elapsedMs: Date.now() - start,
        totalItems,
        totalPages,
    });

    return ResponseHandler.responseBuilder("", responsePageableDTO, 0, StatusCodes.OK, true, "");
}

export async function get(FinanzasPaymentUuid: string) {
    return r.findById(FinanzasPaymentUuid);
}

export async function create(dto: CreateFinanzasPaymentDto) {
    const data: Partial<FinanzasPayment> = {
        company: dto.company,
        documentNumber: dto.documentNumber,
        documentReference: dto.documentReference,
        amount: dto.amount,
        vendorNumber: dto.vendorNumber,
        status: dto.status,
        documentType: dto.documentType,
        sapDocument: dto.sapDocument,
        paymentDate: dto.paymentDate,
        createdAt: new Date(),
        paymentHeaderUuid: dto.paymentHeaderUuid ?? null
    };

    const entityCreated = await r.createOne(data);
    return ResponseHandler.responseBuilder("", entityCreated, 0, StatusCodes.CREATED, true, "");
}

/**
 * Crea cabecera + detalles en una sola transacción.
 * - paymentHeaderUuid se genera en backend (UUID)
 * - totalAmount cabecera = suma(details.amount)
 */
export async function createHeaderWithDetails(dto: CreateFinanzasPaymentHeaderWithDetailsDto) {
    const queryRunner = datasource.createQueryRunner();

    await queryRunner.connect();
    await queryRunner.startTransaction();

    try {
        const headerRepository = queryRunner.manager.getRepository(FinanzasPaymentHeader);
        const detailRepository = queryRunner.manager.getRepository(FinanzasPayment);

        const hasIncome = dto.details.some(d => d.paymentLineType === "INCOME");
        if (!hasIncome) {
            await queryRunner.rollbackTransaction();
            return ResponseHandler.responseBuilder(
                "WRN7025",
                { message: "El desglose del pago no contiene un ingreso, favor de validar." },
                0,
                StatusCodes.BAD_REQUEST,
                false,
                ""
            );
        }

        const sumIncome = dto.details
            .filter(d => d.paymentLineType === "INCOME")
            .reduce((acc, d) => acc + Number(d.amount), 0);

        const sumCreditNotes = dto.details
            .filter(d => d.paymentLineType === "CREDIT_NOTE")
            .reduce((acc, d) => acc + Number(d.amount), 0);

        const expectedTotal = Number((sumIncome - sumCreditNotes).toFixed(2));
        const headerTotal = Number(Number(dto.totalAmount).toFixed(2));

        if (headerTotal !== expectedTotal) {
            await queryRunner.rollbackTransaction();
            return ResponseHandler.responseBuilder(
                "WRN7024",
                { message: "El valor del pago total no es igual al desglose del pago, favor de validar." },
                0,
                StatusCodes.BAD_REQUEST,
                false,
                ""
            );
        }

        const paymentHeaderUuid = randomUUID();
        const now = new Date();

        const header = headerRepository.create({
            paymentHeaderUuid,
            company: dto.company,
            anio: dto.anio,
            vendorNumber: dto.vendorNumber,
            currency: dto.currency,
            totalAmount: headerTotal.toFixed(2),
            paymentDate: dto.paymentDate,
            status: dto.status,
            createdBy: dto.createdBy ?? null,
            createdAt: now,
            updatedBy: null,
            updatedAt: null,
        });

        const headerSaved = await headerRepository.save(header);

        const detailsToSave = dto.details.map(item =>
            detailRepository.create({
                company: dto.company,
                documentNumber: item.documentNumber,
                documentReference: item.documentReference,
                vendorNumber: dto.vendorNumber,
                amount: item.amount,
                currency: dto.currency,
                documentType: item.documentType,
                sapDocument: item.sapDocument,
                paymentDate: dto.paymentDate,
                status: dto.status,
                paymentHeaderUuid,
                createdAt: now,
                createdBy: dto.createdBy ?? null,
                updatedAt: null,
                updatedBy: null,
            })
        );

        const detailsSaved = await detailRepository.save(detailsToSave);

        await queryRunner.commitTransaction();

        return ResponseHandler.responseBuilder(
            "",
            {
                header: headerSaved,
                details: detailsSaved,
                summary: {
                    paymentHeaderUuid,
                    totalDetails: detailsSaved.length,
                    headerTotal: headerTotal.toFixed(2),
                    expectedTotal: expectedTotal.toFixed(2),
                },
            },
            0,
            StatusCodes.CREATED,
            true,
            ""
        );
    } catch (error) {
        await queryRunner.rollbackTransaction();
        throw error;
    } finally {
        await queryRunner.release();
    }
}

/**
 * Obtiene cabecera + detalles por paymentHeaderUuid
 * - Si no envían paginación, regresa todos los detalles
 * - Si envían pageNumber/pageSize, regresa detailsPage paginado
 */
export async function getHeaderWithDetails(paymentHeaderUuid: string, q?: ListFinanzasPaymentDetailsByHeaderQueryDto) {
    const header = await headerRepo.findById(paymentHeaderUuid);

    if (!header) {
        return ResponseHandler.responseBuilder("Header payment not found", null, 0, StatusCodes.NOT_FOUND, false, "");
    }

    if (!q) {
        const details = await r.findByPaymentHeaderUuid(paymentHeaderUuid);

        const totalAmountDetail = details.reduce((acc, item) => acc + Number(item.amount), 0).toFixed(2);

        return ResponseHandler.responseBuilder(
            "",
            {
                header,
                details,
                summary: {
                    paymentHeaderUuid,
                    totalDetails: details.length,
                    totalAmountHeader: header.totalAmount,
                    totalAmountDetail,
                    amountsMatch: Number(header.totalAmount) === Number(totalAmountDetail),
                },
            },
            0,
            StatusCodes.OK,
            true,
            ""
        );
    }

    const [details, total, numberOfElements] = await r.findAllPaginatedByPaymentHeaderUuid(
        paymentHeaderUuid,
        q.pageSize,
        q.pageNumber
    );

    let totalPages = total / q.pageSize;
    totalPages = totalPages - Math.trunc(totalPages) > 0 ? Math.trunc(totalPages) + 1 : Math.trunc(totalPages);

    return ResponseHandler.responseBuilder(
        "",
        {
            header,
            detailsPage: {
                content: details,
                totalElements: total,
                numberOfElements,
                totalPages,
                pageNumber: q.pageNumber,
                pageSize: q.pageSize,
            },
            summary: {
                paymentHeaderUuid,
                totalAmountHeader: header.totalAmount,
            },
        },
        0,
        StatusCodes.OK,
        true,
        ""
    );
}

export async function update(dto: UpdateFinanzasPaymentDto) {
    const filter: FindOptionsWhere<FinanzasPayment> = {};

    if (dto.documentNumber !== undefined) filter.documentNumber = dto.documentNumber;
    if (dto.vendorNumber !== undefined) filter.vendorNumber = dto.vendorNumber;
    if (dto.documentType !== undefined) filter.documentType = dto.documentType;
    if (dto.sapDocument !== undefined) filter.sapDocument = dto.sapDocument;

    const entityUpdated = await r.updateStatus(filter, dto.status);
    let response = ResponseHandler.responseBuilder("", entityUpdated, 0, StatusCodes.OK, true, "");
    if (!entityUpdated) response = ResponseHandler.responseBuilder("NOT FOUND", entityUpdated, 0, StatusCodes.NOT_FOUND, false, "");
    return response;
}
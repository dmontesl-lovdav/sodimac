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
    UpdateFinanzasPaymentPatchDto,
} from "@/schemas/finanzasPayment.schema.js";
import { FinanzasPaymentHeader } from "@/entities/FinanzasPaymentHeader.entity.js";
import { FinanzasPayment } from "@/entities/FinanzasPayment.entities.js";
import {
    Between,
    MoreThanOrEqual,
    type FindOptionsWhere,
} from "typeorm";
import { datasource } from "@/config/typeorm-datasource.js";
import { randomUUID } from "crypto";

export async function list(
    q: ListFinanzasPaymentQuery
) {
    const start = Date.now();

    console.log(
        "[finanzas-payment][service.list] START",
        q
    );

    /*
     * La pantalla principal de pagos trabaja a nivel cabecera.
     *
     * Un payment_header representa un único pago, independientemente
     * de la cantidad de registros existentes en payment_detail.
     */
    const filter: FindOptionsWhere<FinanzasPaymentHeader> =
        {};

    if (q.vendorNumber !== undefined) {
        filter.vendorNumber =
            q.vendorNumber;
    }

    if (q.paymentDate !== undefined) {
        filter.paymentDate =
            MoreThanOrEqual(
                q.paymentDate
            );
    }

    const parsedDateEnd =
        z.coerce.date().parse(
            q.createdAtEnd
        );

    parsedDateEnd.setDate(
        parsedDateEnd.getDate() + 1
    );

    if (
        q.createdAtInitial !==
        undefined
    ) {
        filter.createdAt = Between(
            q.createdAtInitial,
            parsedDateEnd
        );
    }

    console.log(
        "[finanzas-payment][service.list] HEADER_FILTER",
        filter
    );

    const repoStart = Date.now();

    const [
        result,
        total,
        numberOfElements,
    ] =
        await headerRepo.findAllPaginated(
            filter,
            q.pageSize,
            q.pageNumber
        );

    console.log(
        "[finanzas-payment][service.list] HEADER_REPO_DONE",
        {
            repoElapsedMs:
                Date.now() -
                repoStart,
            rows: result.length,
            total,
            numberOfElements,
        }
    );

    /*
     * Se conserva el contrato que actualmente consume el frontend.
     *
     * payment_header.totalAmount se expone como amount para que
     * la pantalla principal muestre el monto real de la cabecera
     * y no el importe de alguno de sus detalles.
     */
    const content = result.map(
        (item) => ({
            id:
                item.paymentHeaderUuid,

            paymentHeaderUuid:
                item.paymentHeaderUuid,

            company:
                item.company,

            anio:
                item.anio,

            documentReference:
                item.documentReference,

            vendorNumber:
                item.vendorNumber,

            amount:
                item.totalAmount,

            currency:
                item.currency,

            paymentDate:
                item.paymentDate,

            status:
                item.status,

            createdBy:
                item.createdBy,

            createdAt:
                item.createdAt,

            updatedBy:
                item.updatedBy,

            updatedAt:
                item.updatedAt,
        })
    );

    const totalItems =
        total?.valueOf() == null
            ? 0
            : Number(total.valueOf());

    let totalPages =
        totalItems / q.pageSize;

    if (
        totalPages -
        Math.trunc(totalPages) >
        0
    ) {
        totalPages =
            Math.trunc(totalPages) + 1;
    } else {
        totalPages =
            Math.trunc(totalPages);
    }

    const responsePageableDTO:
        ResponsePageableDTO = {
        content,
        totalElements:
            totalItems,
        numberOfElements:
            numberOfElements?.valueOf() ==
                null
                ? 0
                : Number(
                    numberOfElements.valueOf()
                ),
        totalPages,
        pageNumber:
            q.pageNumber,
        pageSize:
            q.pageSize,
    };

    console.log(
        "[finanzas-payment][service.list] END",
        {
            elapsedMs:
                Date.now() - start,
            totalItems,
            totalPages,
        }
    );

    return ResponseHandler.responseBuilder(
        "",
        responsePageableDTO,
        0,
        StatusCodes.OK,
        true,
        ""
    );
}

export async function get(
    finanzasPaymentUuid: string
) {
    return r.findById(
        finanzasPaymentUuid
    );
}

/**
 * Crea un detalle de pago.
 *
 * El paymentHeaderUuid puede enviarse para relacionar el detalle con
 * una cabecera existente o puede omitirse.
 */
export async function create(
    dto: CreateFinanzasPaymentDto
) {
    const now = new Date();

    const data: Partial<FinanzasPayment> =
    {
        company: dto.company,
        documentNumber:
            dto.documentNumber,
        documentReference:
            dto.documentReference,
        vendorNumber:
            dto.vendorNumber,
        amount: dto.amount,
        currency:
            dto.currency ?? "MXN",
        documentType:
            dto.documentType,
        sapDocument:
            dto.sapDocument,
        paymentDate:
            dto.paymentDate,
        status: dto.status ?? 0,
        paymentHeaderUuid:
            dto.paymentHeaderUuid ??
            null,
        createdBy:
            dto.createdBy ?? null,
        createdAt: now,
        updatedBy: null,
        updatedAt: null,
    };

    const entityCreated =
        await r.createOne(data);

    return ResponseHandler.responseBuilder(
        "",
        entityCreated,
        0,
        StatusCodes.CREATED,
        true,
        ""
    );
}

/**
 * Crea una cabecera y, opcionalmente, sus detalles en una sola
 * transacción.
 *
 * Reglas:
 * - Si paymentHeaderUuid llega en el request, se conserva.
 * - Si paymentHeaderUuid no llega, se genera un UUID.
 * - details puede omitirse o enviarse vacío.
 * - Las validaciones de INCOME, CREDIT_NOTE y total solamente se
 *   ejecutan cuando existen detalles.
 * - El status predeterminado es 0.
 */
export async function createHeaderWithDetails(
    dto: CreateFinanzasPaymentHeaderWithDetailsDto
) {
    const queryRunner =
        datasource.createQueryRunner();

    await queryRunner.connect();
    await queryRunner.startTransaction();

    try {
        const headerRepository =
            queryRunner.manager.getRepository(
                FinanzasPaymentHeader
            );

        const detailRepository =
            queryRunner.manager.getRepository(
                FinanzasPayment
            );

        const details =
            dto.details ?? [];

        const headerTotal = Number(
            Number(
                dto.totalAmount
            ).toFixed(2)
        );

        let expectedTotal:
            | number
            | null = null;

        /*
         * La validación del desglose solo se ejecuta cuando
         * se reciben detalles.
         */
        if (details.length > 0) {
            const hasIncome =
                details.some(
                    (detail) =>
                        detail.paymentLineType ===
                        "INCOME"
                );

            if (!hasIncome) {
                await queryRunner.rollbackTransaction();

                return ResponseHandler.responseBuilder(
                    "WRN7025",
                    {
                        message:
                            "El desglose del pago no contiene un ingreso, favor de validar.",
                    },
                    0,
                    StatusCodes.BAD_REQUEST,
                    false,
                    ""
                );
            }

            const sumIncome =
                details
                    .filter(
                        (detail) =>
                            detail.paymentLineType ===
                            "INCOME"
                    )
                    .reduce(
                        (
                            accumulator,
                            detail
                        ) =>
                            accumulator +
                            Number(
                                detail.amount
                            ),
                        0
                    );

            const sumCreditNotes =
                details
                    .filter(
                        (detail) =>
                            detail.paymentLineType ===
                            "CREDIT_NOTE"
                    )
                    .reduce(
                        (
                            accumulator,
                            detail
                        ) =>
                            accumulator +
                            Number(
                                detail.amount
                            ),
                        0
                    );

            expectedTotal = Number(
                (
                    sumIncome -
                    sumCreditNotes
                ).toFixed(2)
            );

            if (
                headerTotal !==
                expectedTotal
            ) {
                await queryRunner.rollbackTransaction();

                return ResponseHandler.responseBuilder(
                    "WRN7024",
                    {
                        message:
                            "El valor del pago total no es igual al desglose del pago, favor de validar.",
                    },
                    0,
                    StatusCodes.BAD_REQUEST,
                    false,
                    ""
                );
            }
        }

        /*
         * Permite usar el UUID enviado por el consumidor.
         * Si no llega, conserva el comportamiento anterior y genera uno.
         */
        const paymentHeaderUuid =
            dto.paymentHeaderUuid ??
            randomUUID();

        const existingHeader =
            await headerRepository.findOneBy(
                {
                    paymentHeaderUuid,
                }
            );

        if (existingHeader) {
            await queryRunner.rollbackTransaction();

            return ResponseHandler.responseBuilder(
                "PAYMENT_HEADER_UUID_ALREADY_EXISTS",
                {
                    message:
                        "The supplied paymentHeaderUuid already exists.",
                    paymentHeaderUuid,
                },
                0,
                StatusCodes.CONFLICT,
                false,
                ""
            );
        }

        const now = new Date();
        const status =
            dto.status ?? 0;

        const header =
            headerRepository.create({
                paymentHeaderUuid,
                company: dto.company,
                anio: dto.anio,
                vendorNumber:
                    dto.vendorNumber,
                documentReference:
                    dto.documentReference,
                currency:
                    dto.currency ??
                    "MXN",
                totalAmount:
                    headerTotal.toFixed(
                        2
                    ),
                paymentDate:
                    dto.paymentDate,
                status,
                createdBy:
                    dto.createdBy ??
                    null,
                createdAt: now,
                updatedBy: null,
                updatedAt: null,
            });

        const headerSaved =
            await headerRepository.save(
                header
            );

        /*
         * Cuando no se reciben detalles, no se realiza un insert
         * vacío en el repositorio.
         */
        let detailsSaved:
            FinanzasPayment[] = [];

        if (details.length > 0) {
            const detailsToSave =
                details.map((item) =>
                    detailRepository.create(
                        {
                            company:
                                dto.company,

                            documentNumber:
                                item.documentNumber,

                            documentReference:
                                item.documentReference,

                            /*
                             * UUID fiscal real de la factura / nota de crédito.
                             */
                            uuid:
                                item.uuid ??
                                null,

                            vendorNumber:
                                dto.vendorNumber,

                            amount:
                                item.amount,

                            currency:
                                dto.currency ??
                                "MXN",

                            documentType:
                                item.documentType,

                            sapDocument:
                                item.sapDocument,

                            paymentDate:
                                dto.paymentDate,

                            /*
                             * El detalle puede traer status propio.
                             * Si no llega, hereda el de la cabecera.
                             */
                            status:
                                item.status ??
                                status,

                            paymentHeaderUuid,

                            createdAt:
                                now,

                            createdBy:
                                dto.createdBy ??
                                null,

                            updatedAt:
                                null,

                            updatedBy:
                                null,
                        }
                    )
                );

            detailsSaved =
                await detailRepository.save(
                    detailsToSave
                );
        }

        await queryRunner.commitTransaction();

        return ResponseHandler.responseBuilder(
            "",
            {
                header:
                    headerSaved,
                details:
                    detailsSaved,
                summary: {
                    paymentHeaderUuid,
                    totalDetails:
                        detailsSaved.length,
                    headerTotal:
                        headerTotal.toFixed(
                            2
                        ),

                    /*
                     * Si no hubo detalles todavía no existe
                     * un total calculado de desglose.
                     */
                    expectedTotal:
                        expectedTotal ===
                            null
                            ? null
                            : expectedTotal.toFixed(
                                2
                            ),
                },
            },
            0,
            StatusCodes.CREATED,
            true,
            ""
        );
    } catch (error) {
        if (
            queryRunner.isTransactionActive
        ) {
            await queryRunner.rollbackTransaction();
        }

        throw error;
    } finally {
        await queryRunner.release();
    }
}

/**
 * Obtiene cabecera y detalles mediante paymentHeaderUuid.
 *
 * Si no se envía paginación, devuelve todos los detalles.
 * Si se envía pageNumber/pageSize, devuelve detailsPage.
 */
export async function getHeaderWithDetails(
    paymentHeaderUuid: string,
    q?: ListFinanzasPaymentDetailsByHeaderQueryDto
) {
    const header =
        await headerRepo.findById(
            paymentHeaderUuid
        );

    if (!header) {
        return ResponseHandler.responseBuilder(
            "Header payment not found",
            null,
            0,
            StatusCodes.NOT_FOUND,
            false,
            ""
        );
    }

    if (!q) {
        const details =
            await r.findByPaymentHeaderUuid(
                paymentHeaderUuid
            );

        const totalAmountDetail =
            details
                .reduce(
                    (
                        accumulator,
                        item
                    ) =>
                        accumulator +
                        Number(
                            item.amount
                        ),
                    0
                )
                .toFixed(2);

        return ResponseHandler.responseBuilder(
            "",
            {
                header,
                details,
                summary: {
                    paymentHeaderUuid,
                    totalDetails:
                        details.length,
                    totalAmountHeader:
                        header.totalAmount,
                    totalAmountDetail,
                    amountsMatch:
                        Number(
                            header.totalAmount
                        ) ===
                        Number(
                            totalAmountDetail
                        ),
                },
            },
            0,
            StatusCodes.OK,
            true,
            ""
        );
    }

    const [
        details,
        total,
        numberOfElements,
    ] =
        await r.findAllPaginatedByPaymentHeaderUuid(
            paymentHeaderUuid,
            q.pageSize,
            q.pageNumber
        );

    let totalPages =
        total / q.pageSize;

    totalPages =
        totalPages -
            Math.trunc(
                totalPages
            ) >
            0
            ? Math.trunc(
                totalPages
            ) + 1
            : Math.trunc(
                totalPages
            );

    return ResponseHandler.responseBuilder(
        "",
        {
            header,
            detailsPage: {
                content:
                    details,
                totalElements:
                    total,
                numberOfElements,
                totalPages,
                pageNumber:
                    q.pageNumber,
                pageSize:
                    q.pageSize,
            },
            summary: {
                paymentHeaderUuid,
                totalAmountHeader:
                    header.totalAmount,
            },
        },
        0,
        StatusCodes.OK,
        true,
        ""
    );
}

/**
 * Actualiza parcialmente un detalle mediante su UUID.
 *
 * PATCH /finanzas-payment/:finanzasPaymentUuid
 *
 * Todos los campos del DTO son opcionales. El schema garantiza
 * que al menos uno sea enviado.
 */
export async function update(
    finanzasPaymentUuid: string,
    dto: UpdateFinanzasPaymentPatchDto
) {
    const existing =
        await r.findById(
            finanzasPaymentUuid
        );

    if (!existing) {
        return ResponseHandler.responseBuilder(
            "NOT FOUND",
            null,
            0,
            StatusCodes.NOT_FOUND,
            false,
            ""
        );
    }

    const patch:
        Partial<FinanzasPayment> = {
        updatedAt:
            new Date(),
    };

    if (
        dto.company !==
        undefined
    ) {
        patch.company =
            dto.company;
    }

    if (
        dto.documentNumber !==
        undefined
    ) {
        patch.documentNumber =
            dto.documentNumber;
    }

    if (
        dto.documentReference !==
        undefined
    ) {
        patch.documentReference =
            dto.documentReference;
    }

    if (
        dto.vendorNumber !==
        undefined
    ) {
        patch.vendorNumber =
            dto.vendorNumber;
    }

    if (
        dto.amount !==
        undefined
    ) {
        patch.amount =
            dto.amount;
    }

    if (
        dto.currency !==
        undefined
    ) {
        patch.currency =
            dto.currency;
    }

    if (
        dto.documentType !==
        undefined
    ) {
        patch.documentType =
            dto.documentType;
    }

    if (
        dto.sapDocument !==
        undefined
    ) {
        patch.sapDocument =
            dto.sapDocument;
    }

    if (
        dto.paymentDate !==
        undefined
    ) {
        patch.paymentDate =
            dto.paymentDate;
    }

    /*
     * Se compara contra undefined para permitir status = 0.
     */
    if (
        dto.status !==
        undefined
    ) {
        patch.status =
            dto.status;
    }

    /*
     * Permite asignar, cambiar o quitar la relación lógica.
     */
    if (
        dto.paymentHeaderUuid !==
        undefined
    ) {
        patch.paymentHeaderUuid =
            dto.paymentHeaderUuid;
    }

    if (
        dto.updatedBy !==
        undefined
    ) {
        patch.updatedBy =
            dto.updatedBy;
    }

    const entityUpdated =
        await r.updateOne(
            finanzasPaymentUuid,
            patch
        );

    if (!entityUpdated) {
        return ResponseHandler.responseBuilder(
            "NOT FOUND",
            null,
            0,
            StatusCodes.NOT_FOUND,
            false,
            ""
        );
    }

    return ResponseHandler.responseBuilder(
        "",
        entityUpdated,
        0,
        StatusCodes.OK,
        true,
        ""
    );
}
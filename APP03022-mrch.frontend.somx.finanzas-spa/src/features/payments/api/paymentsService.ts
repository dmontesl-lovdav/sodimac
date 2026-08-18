import {
    PaymentRecord,
    PagedResult,
    PaymentSearchParams,
    PaymentDetail,
    PaymentDocument,
} from "../interfaces";
import { createApiClient } from "@/services/ApiClient";
import { formatDate, parseDisplayDate } from "@/utils/utils";

const api = createApiClient();

const MAX_BACKEND_PAGE_SIZE = 200;

const formatPaymentAmount = (amount: number): string =>
    `$${amount.toLocaleString("es-MX", {
        minimumFractionDigits: 2,
        maximumFractionDigits: 2,
    })}`;

const getDocumentTypeOrder = (documentType?: string): number => {
    const normalized = documentType?.trim().toUpperCase() ?? "";

    if (
        normalized === "FAC" ||
        normalized.includes("FACTURA") ||
        normalized.includes("INVOICE")
    ) {
        return 0;
    }

    if (
        normalized === "NC" ||
        normalized.includes("NOTA") ||
        normalized.includes("CREDIT")
    ) {
        return 1;
    }

    return 2;
};

/**
 * Orden solicitado para el detalle:
 * 1. Facturas.
 * 2. Notas de crédito.
 * 3. Otros tipos.
 *
 * Dentro de cada grupo se ordena por importe de mayor a menor.
 */
export const sortPaymentDocuments = (
    documents: PaymentDocument[]
): PaymentDocument[] =>
    [...documents].sort((left, right) => {
        const typeDifference =
            getDocumentTypeOrder(left.documentType) -
            getDocumentTypeOrder(right.documentType);

        if (typeDifference !== 0) {
            return typeDifference;
        }

        const amountDifference = right.amount - left.amount;

        if (amountDifference !== 0) {
            return amountDifference;
        }

        return left.documentNumber.localeCompare(
            right.documentNumber,
            "es-MX",
            { sensitivity: "base" }
        );
    });

const escapeCsvCell = (value: unknown): string => {
    const text = value == null ? "" : String(value);
    return `"${text.replace(/"/g, '""')}"`;
};

class PaymentsClient {
    private readonly ROUTE = "finanzas-payment";
    private readonly ACCOUNTS_ROUTE = "accounts-payable";

    async searchPayments(
        params: PaymentSearchParams
    ): Promise<PagedResult<PaymentRecord>> {
        const body: Record<string, unknown> = {
            createdAtInitial: params.startDate,
            createdAtEnd: params.endDate,
            pageNumber: params.page ?? 1,
            pageSize: params.size ?? 10,
        };

        if (params.providerId) {
            body.vendorNumber = Number(params.providerId);
        }

        if (params.paymentNumber) {
            body.documentNumber = params.paymentNumber;
        }

        if (params.referenceNumber) {
            body.documentReference = params.referenceNumber;
        }

        if (typeof params.statusId === "number") {
            body.status = params.statusId;
        }

        const wrapper = await api.request<any>(
            this.ROUTE,
            "get",
            undefined,
            { params: body }
        );

        const pageable = wrapper?.data ?? wrapper ?? {};
        const content = pageable?.content ?? [];

        const items: PaymentRecord[] = content.map((item: any) => ({
            idPago: item.finanzasPaymentUuid ?? item.id ?? "",
            paymentHeaderUuid: item.paymentHeaderUuid ?? null,
            documentNumber: item.documentNumber ?? "",
            documentReference: item.documentReference ?? "",
            providerNumber: String(item.vendorNumber ?? ""),
            providerName:
                item.vendorName ??
                item.providerName ??
                item.businessName ??
                item.supplierName ??
                "",
            currency: item.currency ?? "MXN",
            amount: ((numberValue) =>
                Number.isFinite(numberValue) ? numberValue : 0)(
                    Number(item.amount)
                ),
            documentType: item.documentType ?? "",
            sapDocument: item.sapDocument ?? "",
            paymentDate: item.paymentDate
                ? formatDate(item.paymentDate)
                : "",
            paymentYear: item.paymentDate
                ? String(
                    parseDisplayDate(
                        item.paymentDate
                    )?.getFullYear() ?? ""
                )
                : "",
            status: getStatusLabel(item.status),
            statusId: item.status ?? 0,
            createdAt: item.createdAt
                ? formatDate(item.createdAt)
                : "",
            updatedAt: item.updatedAt
                ? formatDate(item.updatedAt)
                : "",
        }));

        return {
            items,
            currentPage:
                pageable?.pageNumber ?? (params.page ?? 1),
            totalItems:
                pageable?.totalElements ?? items.length,
            totalPages:
                pageable?.totalPages ??
                Math.max(
                    1,
                    Math.ceil(
                        (pageable?.totalElements ??
                            items.length) /
                        (params.size ?? 10)
                    )
                ),
        };
    }

    /**
     * Recorre todas las páginas sin superar el máximo de 200
     * permitido por el backend.
     */
    async searchAllPayments(
        params: PaymentSearchParams
    ): Promise<PagedResult<PaymentRecord>> {
        const pageSize = Math.min(
            Math.max(
                params.size ?? MAX_BACKEND_PAGE_SIZE,
                1
            ),
            MAX_BACKEND_PAGE_SIZE
        );

        const firstPage = await this.searchPayments({
            ...params,
            page: 1,
            size: pageSize,
        });

        const allItems: PaymentRecord[] = [
            ...firstPage.items,
        ];

        for (
            let pageNumber = 2;
            pageNumber <= Math.max(1, firstPage.totalPages);
            pageNumber += 1
        ) {
            const nextPage = await this.searchPayments({
                ...params,
                page: pageNumber,
                size: pageSize,
            });

            allItems.push(...nextPage.items);
        }

        return {
            items: allItems,
            currentPage: 1,
            totalItems: allItems.length,
            totalPages: 1,
        };
    }

    /**
     * GET /finanzas-payment/header-with-details/:paymentHeaderUuid
     *
     * Si opts se omite, solicita todos los detalles. Esto permite
     * aplicar el orden global requerido antes de paginar en pantalla
     * y antes de generar el reporte.
     */
    async getHeaderWithDetails(
        paymentHeaderUuid: string,
        opts?: {
            pageNumber: number;
            pageSize: number;
        }
    ): Promise<any> {
        return api.request<any>(
            `${this.ROUTE}/header-with-details/${paymentHeaderUuid}`,
            "get",
            undefined,
            opts
                ? {
                    params: {
                        pageNumber: opts.pageNumber,
                        pageSize: opts.pageSize,
                    },
                }
                : undefined
        );
    }

    async getPaymentDetail(
        paymentNumber: string
    ): Promise<PaymentDetail> {
        const searchResult = await this.searchPayments({
            startDate: "2020-01-01",
            endDate: new Date().toISOString().split("T")[0],
            paymentNumber,
            page: 1,
            size: 1,
        });

        const payment = searchResult.items[0];

        if (!payment) {
            throw new Error("Pago no encontrado");
        }

        let documents: PaymentDocument[] = [];

        try {
            const accountsResponse = await api.request<any>(
                `${this.ACCOUNTS_ROUTE}?vendorNumber=${payment.providerNumber}&documentNumber=${paymentNumber}`,
                "get"
            );

            const accountsData = Array.isArray(
                accountsResponse
            )
                ? accountsResponse
                : accountsResponse?.content ?? [];

            documents = accountsData.map((document: any) => ({
                id:
                    document.accountsPayableUuid ??
                    document.id ??
                    "",
                finanzasPaymentUuid:
                    document.finanzasPaymentUuid ??
                    undefined,
                documentNumber:
                    document.documentNumber ?? "",
                documentType:
                    document.documentType ?? "",
                reference:
                    document.reference ??
                    document.documentReference ??
                    "",
                documentDate: document.documentDate
                    ? formatDate(document.documentDate)
                    : "",
                accountingDate: document.accountingDate
                    ? formatDate(document.accountingDate)
                    : "",
                dueDate: document.dueDate
                    ? formatDate(document.dueDate)
                    : "",
                currency: document.currency ?? "MXN",
                amount: ((numberValue) =>
                    Number.isFinite(numberValue)
                        ? numberValue
                        : 0)(Number(document.amount)),
                serie: document.serie ?? "",
                folio: document.folio ?? "",
                uuid:
                    document.uuid ??
                    document.finanzasPaymentUuid ??
                    "",
                sapDocument:
                    document.sapDocument ?? "",
                paymentDate: document.paymentDate
                    ? formatDate(document.paymentDate)
                    : "",
                status: document.status ?? "Activo",
                createdAt: document.createdAt
                    ? formatDate(document.createdAt)
                    : "",
                updatedAt: document.updatedAt
                    ? formatDate(document.updatedAt)
                    : "",
            }));
        } catch (error) {
            console.error(
                "[PaymentsService] Error fetching accounts:",
                error
            );

            documents = [];
        }

        return {
            ...payment,
            documents: sortPaymentDocuments(documents),
        };
    }

    exportPaymentsCsv(rows: PaymentRecord[]): Blob {
        const headers = [
            "Referencia Pago",
            "Importe",
            "Moneda",
            "Año Pago",
            "Fecha Pago",
            "Número Proveedor",
            "Nombre Proveedor",
            "Fecha Registro",
            "Fecha Actualización",
            "Estatus",
        ];

        const csvRows = rows.map((item) => [
            item.documentReference,
            formatPaymentAmount(item.amount),
            item.currency,
            item.paymentYear,
            formatDate(item.paymentDate),
            item.providerNumber,
            item.providerName,
            item.createdAt,
            item.updatedAt,
            item.status,
        ]);

        const csvContent =
            "\uFEFF" +
            [
                headers.map(escapeCsvCell).join(","),
                ...csvRows.map((row) =>
                    row.map(escapeCsvCell).join(",")
                ),
            ].join("\n");

        return new Blob([csvContent], {
            type: "text/csv;charset=utf-8;",
        });
    }

    async uploadPaymentComplement(
        paymentNumber: string,
        file: File
    ): Promise<void> {
        const formData = new FormData();

        formData.append("file", file);
        formData.append(
            "paymentNumber",
            paymentNumber
        );

        await api.request(
            `${this.ROUTE}/complement`,
            "post",
            formData
        );
    }

    async exportPaymentDetail(
        paymentNumber: string,
        isAdmin = false
    ): Promise<Blob> {
        const detail =
            await this.getPaymentDetail(paymentNumber);

        const headers = [
            ...(isAdmin
                ? [
                    "Número Proveedor",
                    "Nombre Proveedor",
                ]
                : []),
            "Número Pago",
            "Año Pago",
            "Fecha Pago",
            "Monto Pago",
            "Moneda",
            "Número Documento",
            "Referencia",
            "Fecha Documento",
            ...(isAdmin
                ? ["Fecha Contable"]
                : []),
            "Fecha Vencimiento",
            "Monto Documento",
            "Serie",
            "Folio",
            "UUID",
            "Estatus",
        ];

        const rows = detail.documents.map(
            (document) => [
                ...(isAdmin
                    ? [
                        detail.providerNumber,
                        detail.providerName,
                    ]
                    : []),
                detail.documentNumber,
                detail.paymentYear ?? "",
                detail.paymentDate,
                detail.amount.toString(),
                detail.currency,
                document.documentNumber,
                document.reference ?? "",
                document.documentDate,
                ...(isAdmin
                    ? [
                        document.accountingDate ??
                        "",
                    ]
                    : []),
                document.dueDate,
                document.amount.toString(),
                document.serie ?? "",
                document.folio ?? "",
                document.uuid ?? "",
                document.status,
            ]
        );

        const csvContent =
            "\uFEFF" +
            [
                headers.map(escapeCsvCell).join(","),
                ...rows.map((row) =>
                    row.map(escapeCsvCell).join(",")
                ),
            ].join("\n");

        return new Blob([csvContent], {
            type: "text/csv;charset=utf-8;",
        });
    }

    /**
     * Reporte del detalle del pago.
     *
     * Conserva exactamente el orden solicitado en pantalla,
     * elimina Estatus e incluye Nombre Proveedor y UUID.
     */
    exportDetailCsv(
        documents: PaymentDocument[],
        providerName = "",
        providerNumber = "",
        paymentReference = ""
    ): Blob {
        const orderedDocuments =
            sortPaymentDocuments(documents);

        const headers = [
            "Número Proveedor",
            "Nombre Proveedor",
            "Referencia Pago",
            "Número Documento",
            "Documento SAP",
            "UUID",
            "Moneda",
            "Importe",
            "Tipo Documento",
            "Fecha Pago",
            "Fecha Registro",
            "Fecha de Actualización",
            "Factura / NC",
        ];

        const csvRows = orderedDocuments.map(
            (document) => [
                providerNumber,
                providerName,
                paymentReference,
                document.documentNumber,
                document.sapDocument ?? "",
                document.uuid ?? "",
                document.currency,
                formatPaymentAmount(document.amount),
                document.documentType,
                document.paymentDate ??
                document.documentDate ??
                "",
                document.createdAt ?? "",
                document.updatedAt ?? "",
                getDocumentActionLabel(
                    document.documentType
                ),
            ]
        );

        const csvContent =
            "\uFEFF" +
            [
                headers.map(escapeCsvCell).join(","),
                ...csvRows.map((row) =>
                    row.map(escapeCsvCell).join(",")
                ),
            ].join("\n");

        return new Blob([csvContent], {
            type: "text/csv;charset=utf-8;",
        });
    }

    async getMessages(): Promise<
        Record<string, string>
    > {
        return {
            INF6000:
                "No existe información con los criterios establecidos.",
            ERR001:
                "La fecha final no puede exceder un mes desde la fecha actual.",
            ERR002:
                "El periodo máximo de consulta es de 6 meses.",
            ERR003:
                "Fecha inicio es obligatoria.",
            ERR004:
                "Fecha fin es obligatoria.",
            WRN7003:
                "No es posible publicar el complemento de pago, faltan documentos fiscales por publicar",
            SUCCESS001:
                "Búsqueda realizada exitosamente.",
            SUCCESS002:
                "Exportación completada.",
            SUCCESS003:
                "Complemento cargado exitosamente.",
        };
    }
}

function getDocumentActionLabel(
    documentType?: string
): string {
    const normalized =
        documentType?.toLowerCase() ?? "";

    if (
        normalized === "nc" ||
        normalized.includes("nota") ||
        normalized.includes("credito") ||
        normalized.includes("crédito")
    ) {
        return "Nota de crédito";
    }

    return "Factura";
}

function getStatusLabel(
    statusId: number
): string {
    const map: Record<number, string> = {
        0: "Pendiente de complemento",
        1: "Complemento relacionado",
        2: "Pago cancelado",
    };

    return map[statusId] ?? "Desconocido";
}

export const paymentsService =
    new PaymentsClient();
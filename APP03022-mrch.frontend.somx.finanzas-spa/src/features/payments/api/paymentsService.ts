import {
    PaymentRecord,
    PagedResult,
    PaymentSearchParams,
    PaymentDetail,
    PaymentDocument
} from '../interfaces';
import { createApiClient } from '@/services/ApiClient';
import { formatDate, parseDisplayDate } from '@/utils/utils';

const api = createApiClient();

class PaymentsClient {
    private ROUTE = 'finanzas-payment';
    private ACCOUNTS_ROUTE = 'accounts-payable';

    async searchPayments(params: PaymentSearchParams): Promise<PagedResult<PaymentRecord>> {
        const body: Record<string, any> = {
            createdAtInitial: params.startDate,
            createdAtEnd: params.endDate,
            pageNumber: params.page || 1,
            pageSize: params.size || 10,
        };

        if (params.providerId) {
            body.vendorNumber = Number(params.providerId);
        }

        // Estos existen en tu interfaz; se mandan por compatibilidad si el BE los soporta
        if (params.paymentNumber) {
            body.documentNumber = params.paymentNumber;
        }

        if (params.referenceNumber) {
            body.documentReference = params.referenceNumber;
        }

        if (typeof params.statusId === 'number') {
            body.status = params.statusId;
        }

        const wrapper = await api.request<any>(this.ROUTE, 'get', undefined, { params: body });
        // ApiClient ya regresa res.data, pero tu BE responde con ResponseHandler.responseBuilder => { data: pageable }
        const pageable = wrapper?.data ?? wrapper ?? {};
        const content = pageable?.content ?? [];

        const items: PaymentRecord[] = content.map((item: any) => ({
            idPago: item.finanzasPaymentUuid || item.id || '',
            paymentHeaderUuid: item.paymentHeaderUuid ?? null,
            documentNumber: item.documentNumber || '',
            documentReference: item.documentReference || '',
            providerNumber: String(item.vendorNumber || ''),
            providerName: item.vendorName || '',
            currency: item.currency || 'MXN',
            amount: Number(item.amount) || 0,
            documentType: item.documentType || '',
            sapDocument: item.sapDocument || '',
            paymentDate: item.paymentDate ? formatDate(item.paymentDate) : '',
            paymentYear: item.paymentDate
                ? String(parseDisplayDate(item.paymentDate)?.getFullYear() ?? '')
                : '',
            status: getStatusLabel(item.status),
            statusId: item.status ?? 0,
            createdAt: item.createdAt ? formatDate(item.createdAt) : '',
            updatedAt: item.updatedAt ? formatDate(item.updatedAt) : '',
        }));

        return {
            items,
            currentPage: pageable?.pageNumber ?? (params.page || 1),
            totalItems: pageable?.totalElements ?? items.length,
            totalPages:
                pageable?.totalPages ??
                Math.max(
                    1,
                    Math.ceil(
                        (pageable?.totalElements ?? items.length) /
                        (params.size || 10)
                    )
                ),
        };
    }

    // ✅ NUEVO: detalle real paginado (GET /finanzas-payment/header-with-details/:paymentHeaderUuid?pageNumber=&pageSize=)
    async getHeaderWithDetails(
        paymentHeaderUuid: string,
        opts: { pageNumber: number; pageSize: number }
    ): Promise<any> {
        return api.request<any>(
            `${this.ROUTE}/header-with-details/${paymentHeaderUuid}`,
            'get',
            undefined,
            {
                params: {
                    pageNumber: opts.pageNumber,
                    pageSize: opts.pageSize,
                },
            }
        );
    }

    async getPaymentDetail(paymentNumber: string): Promise<PaymentDetail> {
        const searchResult = await this.searchPayments({
            startDate: '2020-01-01',
            endDate: new Date().toISOString().split('T')[0],
            paymentNumber,
            page: 1,
            size: 1,
        });

        const payment = searchResult.items[0];
        if (!payment) {
            throw new Error('Pago no encontrado');
        }

        let documents: PaymentDocument[] = [];

        try {
            const accountsResponse = await api.request<any>(
                `${this.ACCOUNTS_ROUTE}?vendorNumber=${payment.providerNumber}&documentNumber=${paymentNumber}`,
                'get'
            );

            const accountsData = Array.isArray(accountsResponse)
                ? accountsResponse
                : accountsResponse?.content ?? [];

            documents = accountsData.map((doc: any) => ({
                id: doc.accountsPayableUuid || doc.id || '',
                documentNumber: doc.documentNumber || '',
                documentType: doc.documentType || '',
                reference: doc.reference || '',
                documentDate: doc.documentDate ? formatDate(doc.documentDate) : '',
                accountingDate: doc.accountingDate ? formatDate(doc.accountingDate) : '',
                dueDate: doc.dueDate ? formatDate(doc.dueDate) : '',
                currency: doc.currency || 'MXN',
                amount: Number(doc.amount) || 0,
                serie: doc.serie || '',
                folio: doc.folio || '',
                uuid: doc.uuid || '',
                sapDocument: doc.sapDocument || '',
                paymentDate: doc.paymentDate ? formatDate(doc.paymentDate) : '',
                status: doc.status || 'Activo',
                createdAt: doc.createdAt ? formatDate(doc.createdAt) : '',
                updatedAt: doc.updatedAt ? formatDate(doc.updatedAt) : '',
            }));
        } catch (err) {
            console.error('[PaymentsService] Error fetching accounts:', err);
            documents = [];
        }

        return {
            ...payment,
            documents,
        };
    }

    exportPaymentsCsv(rows: PaymentRecord[]): Blob {
        /** Misma presentación que la columna «Fecha Pago» del grid de consulta. */
        /** Encabezados y orden idénticos al grid de ResultsTable (sin columna Acción). */
        const headers = [
            'Referencia Pago',
            'Importe',
            'Moneda',
            'Año Pago',
            'Fecha Pago',
            'Número Proveedor',
            'Nombre Proveedor',
            'Fecha Registro',
            'Fecha Actualización',
            'Estatus',
        ];

        const formatAmount = (amount: number): string =>
            `$${amount.toLocaleString('es-MX', {
                minimumFractionDigits: 2,
                maximumFractionDigits: 2,
            })}`;

        const csvRows = rows.map(item => [
            item.documentReference,
            formatAmount(item.amount),
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
            '\uFEFF' +
            [
                headers.join(','),
                ...csvRows.map(row => row.map(cell => `"${cell}"`).join(',')),
            ].join('\n');

        return new Blob([csvContent], {
            type: 'text/csv;charset=utf-8;',
        });
    }

    async uploadPaymentComplement(paymentNumber: string, file: File): Promise<void> {
        const formData = new FormData();
        formData.append('file', file);
        formData.append('paymentNumber', paymentNumber);

        await api.request(`${this.ROUTE}/complement`, 'post', formData);
    }

    async exportPaymentDetail(paymentNumber: string, isAdmin: boolean = false): Promise<Blob> {
        const detail = await this.getPaymentDetail(paymentNumber);

        const headers = [
            ...(isAdmin ? ['Número Proveedor', 'Nombre Proveedor'] : []),
            'Número Pago',
            'Año Pago',
            'Fecha Pago',
            'Monto Pago',
            'Moneda',
            'Número Documento',
            'Referencia',
            'Fecha Documento',
            ...(isAdmin ? ['Fecha Contable'] : []),
            'Fecha Vencimiento',
            'Monto Documento',
            'Serie',
            'Folio',
            'UUID',
            'Estatus',
        ];

        const rows = detail.documents.map(doc => [
            ...(isAdmin ? [detail.providerNumber, detail.providerName] : []),
            detail.documentNumber,
            detail.paymentYear || '',
            detail.paymentDate,
            detail.amount.toString(),
            detail.currency,
            doc.documentNumber,
            doc.reference || '',
            doc.documentDate,
            ...(isAdmin ? [doc.accountingDate || ''] : []),
            doc.dueDate,
            doc.amount.toString(),
            doc.serie || '',
            doc.folio || '',
            doc.uuid || '',
            doc.status,
        ]);

        const csvContent = [
            headers.join(','),
            ...rows.map(row => row.map(cell => `"${cell}"`).join(',')),
        ].join('\n');

        return new Blob([csvContent], {
            type: 'text/csv;charset=utf-8;',
        });
    }

    exportDetailCsv(documents: PaymentDocument[]): Blob {
        const headers = [
            'Número documento',
            'Referencia documento',
            'Moneda',
            'Importe',
            'Tipo de documento',
            'Documento SAP',
            'Fecha de pago',
            'Estatus',
            'Fecha de registro',
            'Fecha de actualización',
        ];

        const formatAmount = (amount: number): string =>
            `$${amount.toLocaleString('es-MX', {
                minimumFractionDigits: 2,
                maximumFractionDigits: 2,
            })}`;

        const csvRows = documents.map(doc => [
            doc.documentNumber,
            doc.reference || '',
            doc.currency,
            formatAmount(doc.amount),
            doc.documentType,
            doc.sapDocument || '',
            doc.paymentDate || '',
            doc.status,
            doc.createdAt || '',
            doc.updatedAt || '',
        ]);

        const csvContent =
            '\uFEFF' +
            [
                headers.join(','),
                ...csvRows.map(row => row.map(cell => `"${cell}"`).join(',')),
            ].join('\n');

        return new Blob([csvContent], {
            type: 'text/csv;charset=utf-8;',
        });
    }

    async getMessages(): Promise<Record<string, string>> {
        return {
            INF6000: 'No existe información con los criterios establecidos.',
            ERR001: 'La fecha final no puede exceder un mes desde la fecha actual.',
            ERR002: 'El periodo máximo de consulta es de 6 meses.',
            ERR003: 'Fecha inicio es obligatoria.',
            ERR004: 'Fecha fin es obligatoria.',
            WRN7003:
                'No es posible publicar el complemento de pago, faltan documentos fiscales por publicar',
            SUCCESS001: 'Búsqueda realizada exitosamente.',
            SUCCESS002: 'Exportación completada.',
            SUCCESS003: 'Complemento cargado exitosamente.',
        };
    }
}

function getStatusLabel(statusId: number): string {
    const map: Record<number, string> = {
        0: 'Pendiente de complemento',
        1: 'Complemento relacionado',
        2: 'Pago cancelado',
    };
    return map[statusId] ?? 'Desconocido';
}

export const paymentsService = new PaymentsClient();
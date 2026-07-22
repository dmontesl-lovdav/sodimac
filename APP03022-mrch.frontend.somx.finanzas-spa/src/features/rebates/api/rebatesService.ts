import { createApiClient } from '@/services/ApiClient';
import { DiscountRecord, PagedResult, SearchParams } from '../interfaces';

const api = createApiClient();
const ROUTE = 'rebates';

export const rebatesService = {
    async searchDiscounts(
        params: SearchParams
    ): Promise<PagedResult<DiscountRecord>> {
        const body = {
            idProveedor: params.vendorId ?? '',
            numeroDocumento: params.documentNumber ?? undefined,
            referenciaDocumento: params.sapDocument ?? undefined,
            uuid: undefined,
            page: (params.page ?? 1) - 1,
            size: params.size ?? 20,
        };

        const response = await api.request<any>(
            `${ROUTE}/filter`,
            'post',
            body
        );

        const data = response?.data ?? response;
        const content = Array.isArray(data)
            ? data
            : data?.data ?? [];

        const items: DiscountRecord[] = content.map((item: any) => ({
            id: item.id ?? 0,
            vendorNumber:
                item.vendorNumber ?? item.idProveedor ?? '',
            vendorName:
                item.vendorName ?? item.nombreProveedor ?? '',
            documentNumber:
                item.documentNumber ?? item.numeroDocumento ?? '',
            referenceDocument:
                item.referenceDocument ??
                item.referenciaDocumento ??
                '',
            rebateType:
                item.rebateType ?? item.tipoDescuento ?? '',
            sapDocument:
                item.sapDocument ?? item.documentoSap ?? '',
            amount: ((n) => (Number.isFinite(n) ? n : 0))(Number(item.amount ?? item.monto)),
            periodId: item.periodId ?? 0,
            periodName: item.periodName ?? '',
            status: item.status ?? item.estatus ?? '',
            applyDate:
                item.applyDate ?? item.fechaAplicacion ?? '',
            expirationDate:
                item.expirationDate ??
                item.fechaVencimiento ??
                '',
        }));

        return {
            items,
            totalItems:
                data?.total ??
                data?.totalElements ??
                items.length,
            totalPages: data?.totalPages ?? 1,
            page:
                (data?.page ??
                    data?.pageNumber ??
                    0) + 1,
        };
    },
};

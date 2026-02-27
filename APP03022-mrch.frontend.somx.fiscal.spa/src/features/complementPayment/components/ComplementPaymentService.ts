import axios from 'axios';

export const API_BASE_URL = process.env.API_BASE_URL || 'http://localhost:8082';

export interface ComplementPayment {
    paymentsUuid: string;
    fiscalUuid:string;
    series: string;
    folio: string;
    subtotal: number;
    totalAmount: number;
    issuerRfc: string;
    issuerName: string;
    receiverRfc: string;
    receiverName: string;
    paymentDate: string;
    createdAt: string;
    statusDescription: string;
    relatedDocumentsCount?: number;
}

export async function fetchComplementPayments(filters: any, page: number, perPage: number) {
    const params = new URLSearchParams();
    if (filters.uuid) params.append('paymentsUuid', filters.uuid);
    if (filters.serie) params.append('series', filters.serie);
    if (filters.folio) params.append('folio', filters.folio);
    if (filters.rfcEmisor) params.append('rfcEmisor', filters.rfcEmisor);
    if (filters.rfcReceptor) params.append('rfcReceptor', filters.rfcReceptor);
    if (filters.fechaPagoInicio) params.append('fechaPagoInicio', filters.fechaPagoInicio);
    if (filters.fechaPagoFin) params.append('fechaPagoFin', filters.fechaPagoFin);
    if (filters.fechaEmisionInicio) params.append('fechaEmisionInicio', filters.fechaEmisionInicio);
    if (filters.fechaEmisionFin) params.append('fechaEmisionFin', filters.fechaEmisionFin);
    if (filters.status) params.append('status', filters.status);

    params.append('page', page.toString());
    params.append('size', perPage.toString());

    const response = await axios.get(
        `${API_BASE_URL}fiscal/complementos-pago/buscar?${params.toString()}`,
        { headers: { Accept: 'application/json' } }
    );
    return response.data;
}

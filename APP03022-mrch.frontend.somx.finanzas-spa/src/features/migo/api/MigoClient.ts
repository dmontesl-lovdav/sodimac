import { createApiClient } from '../../../services/ApiClient';
import type {
    MigoDocumentPage,
    MigoReceptionPage,
    MigoDocument,
    MigoSearchFilters,
} from '../interfaces';

const api = createApiClient();
const ROUTE = 'migo';

function toQuery(filters: MigoSearchFilters): string {
    const params = new URLSearchParams();
    if (filters.publishedAtStart) params.append('publishedAtStart', filters.publishedAtStart);
    if (filters.publishedAtEnd) params.append('publishedAtEnd', filters.publishedAtEnd);
    if (filters.fileName) params.append('fileName', filters.fileName);
    params.append('pageNumber', String(filters.pageNumber));
    params.append('pageSize', String(filters.pageSize));
    return params.toString();
}

export const migoService = {
    async search(filters: MigoSearchFilters) {
        const qs = toQuery(filters);
        const res = await api.request<{ data: MigoDocumentPage }>(`${ROUTE}?${qs}`, 'get');
        return res;
    },

    async getById(id: string) {
        const res = await api.request<{ data: MigoDocument }>(`${ROUTE}/${id}`, 'get');
        return res;
    },

    async getReceptions(migoDocumentId: string, pageNumber: number, pageSize: number) {
        const params = new URLSearchParams({
            pageNumber: String(pageNumber),
            pageSize: String(pageSize),
        });
        const res = await api.request<{ data: MigoReceptionPage }>(
            `${ROUTE}/${migoDocumentId}/receptions?${params.toString()}`,
            'get'
        );
        return res;
    },

    async uploadCsv(file: File) {
        const formData = new FormData();
        formData.append('file', file);
        const res = await api.request<any>(`${ROUTE}/upload`, 'post', formData);
        return res;
    },

    async authorize(id: string) {
        const res = await api.request<any>(`${ROUTE}/${id}/authorize`, 'patch');
        return res;
    },

    async reject(id: string, rejectionReason: string) {
        const res = await api.request<any>(`${ROUTE}/reject`, 'patch', {
            migoDocumentId: id,
            rejectionReason,
        });
        return res;
    },

    async exportCsv(id: string) {
        await api.requestBinary(`${ROUTE}/${id}/export-csv`, 'get', undefined, `recepciones-migo-${id}.csv`);
    },
};

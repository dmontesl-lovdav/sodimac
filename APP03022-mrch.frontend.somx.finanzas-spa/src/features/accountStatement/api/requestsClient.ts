import { createApiClient } from '@/services/ApiClient';
import type { CreateClarificationRequestBody } from '../interfaces/clarificationRequest';

function getRequestsApiBaseUrl(): string {
    const url = (
        process.env.REQUESTS_API_URL ??
        process.env.REACT_APP_REQUESTS_API_URL ??
        ''
    )
        .trim()
        .replace(/\/+$/, '');

    if (!url) {
        throw new Error(
            'REQUESTS_API_URL no está configurada. Ej.: https://uat.fbusinesscenter.com/ppsomx/backend'
        );
    }

    return url;
}

/**
 * POST {REQUESTS_API_URL}/requests
 * Authorization: Bearer token del usuario/proveedor (correo tomado del token en backend).
 */
export const RequestsClientService = {
    async createClarification(
        body: CreateClarificationRequestBody
    ): Promise<unknown> {
        const api = createApiClient({ baseUrl: getRequestsApiBaseUrl() });
        return api.request<unknown>('requests', 'post', body);
    },
};

/**
 * ============================================================================
 * HTTP CLIENT: fiscal-api
 * ============================================================================
 * Cliente HTTP para comunicarse con fiscal-api (Java/Spring Boot)
 *
 * RESPONSABILIDAD:
 * - Llamar endpoint de validación de NC en fiscal-api
 * - Transformar requests/responses entre TypeScript y Java
 * - Manejo de errores HTTP
 *
 * ENDPOINT USADO:
 * - POST /api/invoices/validate-nc-relation
 *
 * @author Sodimac Tech Team
 * @version 2.0 (Express)
 * @since 2025
 */

import axios, { AxiosError } from 'axios';
import FormData from 'form-data';

export interface NCValidationRequest {
    invoiceFiscalUuid: string;
}

export interface NCValidationResponse {
    valid: boolean;
    ncFiscalUuid?: string;
    relatedInvoiceUuids?: string[];
    relationshipType?: string;
    errorMessage?: string;
    businessCode: string;
}

export class FiscalApiClient {
    private readonly fiscalApiUrl: string;

    constructor() {
        // URL de fiscal-api desde variable de entorno
        this.fiscalApiUrl = process.env.FISCAL_API_URL || 'http://localhost:8080';
        console.log(`[FiscalApiClient] URL configured: ${this.fiscalApiUrl}`);
    }

    /**
     * Valida que un XML de Nota de Crédito está relacionado con una factura
     *
     * @param request Contiene el UUID fiscal de la factura
     * @param xmlFile Buffer del archivo XML de la NC
     * @returns NCValidationResponse con resultado de validación
     */
    async validateNCRelation(
        request: NCValidationRequest,
        xmlFile: Buffer
    ): Promise<NCValidationResponse> {
        try {
            console.log(`[FiscalApiClient] Validating NC relation for invoice: ${request.invoiceFiscalUuid}`);

            // Crear FormData para multipart/form-data
            const formData = new FormData();
            formData.append('invoiceFiscalUuid', request.invoiceFiscalUuid);
            formData.append('xmlFile', xmlFile, {
                filename: 'nc.xml',
                contentType: 'application/xml'
            });

            // Llamar a fiscal-api
            const url = `${this.fiscalApiUrl}/api/invoices/validate-nc-relation`;
            const response = await axios.post<NCValidationResponse>(url, formData, {
                headers: formData.getHeaders(),
                timeout: 30000 // 30 segundos
            });

            console.log(`[FiscalApiClient] Validation result: ${response.data.valid}, code: ${response.data.businessCode}`);
            return response.data;

        } catch (error: any) {
            console.error(`[FiscalApiClient] Error validating NC relation: ${error.message}`);

            // Si fiscal-api retornó error HTTP pero con response body
            if (axios.isAxiosError(error) && error.response?.data) {
                return error.response.data as NCValidationResponse;
            }

            // Error de red o timeout
            return {
                valid: false,
                businessCode: 'FISCAL_API_ERROR',
                errorMessage: `Error al comunicarse con fiscal-api: ${error.message}`
            };
        }
    }
}

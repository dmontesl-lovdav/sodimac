import { createApiClient } from "@/services/ApiClient";

const api = createApiClient();

export interface VersionCatalogDto {
    versionId: number;
    name: string;
    description: string;
    version: string;
    documentType: string;
    pacId: number | null;
    validFrom: string | null;
    validTo: string | null;
    structureUrl: string | null;
    status: number;
}

export const versionCatalogService = {
    getVersionCatalog(): Promise<VersionCatalogDto[]> {
        return api.request<VersionCatalogDto[]>("version-catalog", "get");
    },

    async checkConnection(): Promise<{
        online: boolean;
        message: string;
        count?: number;
    }> {
        try {
            const response = await versionCatalogService.getVersionCatalog();

            const recordsNote = Array.isArray(response) ? ` Registros detectados: ${response.length}.` : "";
            return {
                online: true,
                message: `La conexión con el backend fiscal se encuentra activa. Servicio disponible correctamente.${recordsNote}`,
                count: Array.isArray(response) ? response.length : 0,
            };
        } catch (error: any) {
            return {
                online: false,
                message:
                    error?.message ?? "No fue posible establecer conexión con el backend fiscal.",
            };
        }
    },
};
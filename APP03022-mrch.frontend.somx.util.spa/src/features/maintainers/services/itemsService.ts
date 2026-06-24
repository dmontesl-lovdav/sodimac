import { createApiClient } from '@/services/apiClient';

const api = createApiClient();

export interface ItemDto {
    idItem: number;
    name: string;
    description: string;
    createdBy: number;
    createdAt: string;
    updatedBy: number;
    updatedAt: string;
}

export interface ItemsResponse {
    success: boolean;
    data: ItemDto[];
    count: number;
}

class ItemsService {
    private readonly ROUTE = 'items';

    async getItems(name?: string): Promise<ItemsResponse> {
        return api.request<ItemsResponse>(this.ROUTE, 'get', undefined, {
            params: name ? { name } : undefined,
        });
    }

    async checkConnection(): Promise<{
        online: boolean;
        message: string;
        count?: number;
    }> {
        try {
            const response = await this.getItems();

            if (response?.success) {
                return {
                    online: true,
                    message: `La conexión con el backend se encuentra activa. Servicio disponible correctamente.${typeof response.count === 'number' ? ` Registros detectados: ${response.count}.` : ''}`,
                    count: response.count,
                };
            }

            return {
                online: false,
                message: 'El servicio respondió, pero no regresó una respuesta válida.',
            };
        } catch (error: any) {
            return {
                online: false,
                message:
                    error?.message ||
                    'No fue posible establecer conexión con el backend.',
            };
        }
    }
}

export const itemsService = new ItemsService();
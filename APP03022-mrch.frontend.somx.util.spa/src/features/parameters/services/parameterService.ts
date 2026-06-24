import apiClient from '@/services/apiClient';
import type {
    ApiResponse,
    PaginatedResponse,
    BackendParameter,
    CreateParameterDto,
    UpdateParameterDto,
    CreateVersionDto,
    ParameterListParams,
    Parameter,
    ParameterStatus,
} from '../types';

export interface CatalogItem {
    value: string;
    label: string;
}

export interface ParameterMetadata {
    modules: CatalogItem[];
    parameterTypes: CatalogItem[];
    statuses: CatalogItem[];
}

// Module type from backend
interface BackendModule {
    idModule: number;
    name: string;
    description?: string;
}

// ItemType type from backend
interface BackendItemType {
    idItemType: number;
    name: string;
    description?: string;
}

/**
 * Maps backend parameter to frontend Parameter type
 */
const mapBackendToFrontend = (bp: BackendParameter): Parameter => ({
    id: `PARAM-${bp.idParameter.toString().padStart(2, '0')}`,
    idParameter: bp.idParameter,
    idModule: bp.idModule,
    idType: bp.idType,
    name: bp.name,
    description: bp.description || '',
    module: bp.module?.name || String(bp.idModule),
    parameterType: bp.type?.name || String(bp.idType),
    value: bp.value,
    version: bp.version,
    startDate: bp.startDate?.split('T')[0] ?? '',
    endDate: bp.endDate?.split('T')[0] ?? null,
    status: (Number(bp.status) === 1 ? 1 : 0) as ParameterStatus,
    createdBy: bp.createdBy != null ? String(bp.createdBy) : 'SYSTEM',
    createdAt: bp.createdAt?.split('T')[0] ?? '',
    updatedBy: bp.updatedBy != null ? String(bp.updatedBy) : null,
    updatedAt: bp.updatedAt?.split('T')[0] || null,
});

export const parameterService = {
    /**
     * Obtiene los catálogos necesarios para los filtros de parámetros.
     * Hace llamadas a módulos y tipos de items del backend.
     * Es resiliente a errores: si una llamada falla, devuelve array vacío para ese catálogo.
     */
    getMetadata: async (): Promise<ParameterMetadata> => {
        const [modulesResult, typesResult] = await Promise.allSettled([
            apiClient.request<{ data: BackendModule[]; count: number }>('/modules', 'get'),
            apiClient.request<{ data: BackendItemType[]; count: number }>('/item-types', 'get'),
        ]);

        let modules: CatalogItem[] = [];
        if (modulesResult.status === 'fulfilled' && modulesResult.value?.data) {
            modules = modulesResult.value.data.map((m) => ({
                value: String(m.idModule),
                label: m.name,
            }));
        } else {
            console.error(
                'Error al cargar módulos:',
                modulesResult.status === 'rejected' ? modulesResult.reason : 'Sin datos'
            );
        }

        let parameterTypes: CatalogItem[] = [];
        if (typesResult.status === 'fulfilled' && typesResult.value?.data) {
            parameterTypes = typesResult.value.data.map((t) => ({
                value: String(t.idItemType),
                label: t.name,
            }));
        } else {
            console.error(
                'Error al cargar tipos de parámetro:',
                typesResult.status === 'rejected' ? typesResult.reason : 'Sin datos'
            );
        }

        return {
            modules,
            parameterTypes,
            statuses: [
                { value: '1', label: 'Activo' },
                { value: '0', label: 'Inactivo' },
            ],
        };
    },

    /**
     * Lista parámetros con filtros y paginación
     */
    list: async (params: ParameterListParams = {}): Promise<{
        data: Parameter[];
        total: number;
        page: number;
        limit: number;
        totalPages: number;
    }> => {
        console.log('[parameterService.list] Enviando parámetros al backend:', params);

        const response = await apiClient.request<PaginatedResponse<BackendParameter>>(
            '/parameters',
            'get',
            undefined,
            { params }
        );

        console.log('[parameterService.list] Respuesta del backend:', {
            total: response.total,
            dataCount: response.data?.length,
            firstItems: response.data?.slice(0, 3).map((p) => ({
                id: p.idParameter,
                name: p.name,
                idModule: p.idModule,
                idType: p.idType,
                moduleName: p.module?.name,
            })),
        });

        return {
            data: response.data.map(mapBackendToFrontend),
            total: response.total,
            page: response.page,
            limit: response.limit,
            totalPages: response.totalPages,
        };
    },

    /**
     * Obtiene un parámetro por ID
     */
    getById: async (id: number): Promise<Parameter> => {
        const response = await apiClient.request<ApiResponse<BackendParameter>>(
            `/parameters/${id}`,
            'get'
        );
        return mapBackendToFrontend(response.data);
    },

    /**
     * Crea un nuevo parámetro
     */
    create: async (data: CreateParameterDto): Promise<Parameter> => {
        const response = await apiClient.request<ApiResponse<BackendParameter>>(
            '/parameters',
            'post',
            data
        );
        return mapBackendToFrontend(response.data);
    },

    /**
     * Actualiza metadatos del parámetro (NO permite cambiar value)
     */
    update: async (id: number, data: UpdateParameterDto): Promise<Parameter> => {
        const response = await apiClient.request<ApiResponse<BackendParameter>>(
            `/parameters/${id}`,
            'patch',
            data
        );
        return mapBackendToFrontend(response.data);
    },

    /**
     * Elimina un parámetro (soft delete)
     */
    remove: async (id: number): Promise<void> => {
        await apiClient.request<void>(`/parameters/${id}`, 'delete');
    },

    /**
     * Obtiene el historial de versiones de un parámetro
     */
    getVersions: async (id: number): Promise<Parameter[]> => {
        const response = await apiClient.request<ApiResponse<BackendParameter[]>>(
            `/parameters/${id}/versions`,
            'get'
        );
        return response.data.map(mapBackendToFrontend);
    },

    /**
     * Crea una nueva versión del parámetro (cuando cambia el valor)
     */
    createVersion: async (id: number, data: CreateVersionDto): Promise<Parameter> => {
        const response = await apiClient.request<ApiResponse<BackendParameter>>(
            `/parameters/${id}/versions`,
            'post',
            data
        );
        return mapBackendToFrontend(response.data);
    },

    /**
     * Actualiza el estatus del parámetro
     */
    updateStatus: async (id: number, status: number): Promise<Parameter> => {
        const response = await apiClient.request<ApiResponse<BackendParameter>>(
            `/parameters/${id}/status`,
            'patch',
            { status }
        );
        return mapBackendToFrontend(response.data);
    },

    /**
     * Verifica si un nombre de parámetro ya existe
     */
    checkNameExists: async (name: string): Promise<boolean> => {
        const response = await apiClient.request<PaginatedResponse<BackendParameter>>(
            '/parameters',
            'get',
            undefined,
            { params: { name, limit: 1 } }
        );
        return response.data.length > 0;
    },
};
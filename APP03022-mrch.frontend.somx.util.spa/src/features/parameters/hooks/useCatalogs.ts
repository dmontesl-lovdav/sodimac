import { useQuery } from '@tanstack/react-query';
import { parameterService, type CatalogItem } from '../services/parameterService';

// Query key for catalogs/metadata
export const catalogKeys = {
    all: ['catalogs'] as const,
    metadata: () => [...catalogKeys.all, 'metadata'] as const,
};

export interface CatalogData {
    modules: CatalogItem[];
    parameterTypes: CatalogItem[];
    statuses: CatalogItem[];
}

/**
 * Hook para obtener los catálogos de metadatos (módulos, tipos, estados)
 */
export const useCatalogs = () => {
    return useQuery({
        queryKey: catalogKeys.metadata(),
        queryFn: () => parameterService.getMetadata(),
        staleTime: 5 * 60 * 1000, // 5 minutes - catalogs don't change often
        gcTime: 30 * 60 * 1000, // 30 minutes cache time
        refetchOnWindowFocus: false,
        refetchOnMount: false,
    });
};

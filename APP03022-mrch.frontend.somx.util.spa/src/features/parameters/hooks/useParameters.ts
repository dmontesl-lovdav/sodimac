import { useQuery, useMutation, useQueryClient } from '@tanstack/react-query';
import { parameterService } from '../services/parameterService';
import type {
    ParameterListParams,
    CreateParameterDto,
    UpdateParameterDto,
    CreateVersionDto,
} from '../types';

// Query keys for cache management
export const parameterKeys = {
    all: ['parameters'] as const,
    lists: () => [...parameterKeys.all, 'list'] as const,
    list: (params: ParameterListParams) => [...parameterKeys.lists(), params] as const,
    details: () => [...parameterKeys.all, 'detail'] as const,
    detail: (id: number) => [...parameterKeys.details(), id] as const,
    versions: (id: number) => [...parameterKeys.all, 'versions', id] as const,
};

/**
 * Hook para listar parámetros con filtros y paginación
 */
export const useParameters = (params: ParameterListParams = {}, enabled = true) => {
    return useQuery({
        queryKey: parameterKeys.list(params),
        queryFn: () => parameterService.list(params),
        enabled,
        staleTime: 30 * 1000, // 30 seconds
        gcTime: 5 * 60 * 1000, // 5 minutes cache time
        refetchOnWindowFocus: false,
        refetchOnMount: false,
        placeholderData: (previousData) => previousData,
    });
};

/**
 * Hook para obtener un parámetro por ID
 */
export const useParameter = (id: number, enabled = true) => {
    return useQuery({
        queryKey: parameterKeys.detail(id),
        queryFn: () => parameterService.getById(id),
        enabled: enabled && id > 0,
    });
};

/**
 * Hook para obtener el historial de versiones de un parámetro
 */
export const useParameterVersions = (id: number, enabled = true) => {
    return useQuery({
        queryKey: parameterKeys.versions(id),
        queryFn: () => parameterService.getVersions(id),
        enabled: enabled && id > 0,
    });
};

/**
 * Hook para crear un nuevo parámetro
 */
export const useCreateParameter = () => {
    const queryClient = useQueryClient();

    return useMutation({
        mutationFn: (data: CreateParameterDto) => parameterService.create(data),
        onSuccess: () => {
            // Invalida todas las listas de parámetros para que se refresquen
            queryClient.invalidateQueries({ queryKey: parameterKeys.lists() });
        },
    });
};

/**
 * Hook para actualizar metadatos de un parámetro
 */
export const useUpdateParameter = () => {
    const queryClient = useQueryClient();

    return useMutation({
        mutationFn: ({ id, data }: { id: number; data: UpdateParameterDto }) =>
            parameterService.update(id, data),
        onSuccess: (updatedParameter, { id }) => {
            // Actualiza el caché del parámetro específico
            queryClient.setQueryData(parameterKeys.detail(id), updatedParameter);
            // Invalida las listas
            queryClient.invalidateQueries({ queryKey: parameterKeys.lists() });
        },
    });
};

/**
 * Hook para eliminar un parámetro
 */
export const useDeleteParameter = () => {
    const queryClient = useQueryClient();

    return useMutation({
        mutationFn: (id: number) => parameterService.remove(id),
        onSuccess: (_, id) => {
            // Remueve del caché
            queryClient.removeQueries({ queryKey: parameterKeys.detail(id) });
            // Invalida las listas
            queryClient.invalidateQueries({ queryKey: parameterKeys.lists() });
        },
    });
};

/**
 * Hook para crear una nueva versión de un parámetro
 */
export const useCreateParameterVersion = () => {
    const queryClient = useQueryClient();

    return useMutation({
        mutationFn: ({ id, data }: { id: number; data: CreateVersionDto }) =>
            parameterService.createVersion(id, data),
        onSuccess: (_, { id }) => {
            // Invalida las versiones y listas
            queryClient.invalidateQueries({ queryKey: parameterKeys.versions(id) });
            queryClient.invalidateQueries({ queryKey: parameterKeys.lists() });
        },
    });
};

/**
 * Hook para actualizar el estatus de un parámetro
 */
export const useUpdateParameterStatus = () => {
    const queryClient = useQueryClient();

    return useMutation({
        mutationFn: ({ id, status }: { id: number; status: number }) =>
            parameterService.updateStatus(id, status),
        onSuccess: (updatedParameter, { id }) => {
            queryClient.setQueryData(parameterKeys.detail(id), updatedParameter);
            queryClient.invalidateQueries({ queryKey: parameterKeys.lists() });
        },
    });
};

/**
 * Hook para verificar si un nombre de parámetro existe
 */
export const useCheckParameterNameExists = () => {
    return useMutation({
        mutationFn: (name: string) => parameterService.checkNameExists(name),
    });
};

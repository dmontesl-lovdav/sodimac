import ConfigurationBuilder from '@/configuration/ConfigurationBuilder';
import type ModuleResolver from './ModuleResolver';

const client = ConfigurationBuilder.client;

/* ========================================================= */
/*                  PAGINADO: TODOS LOS RESOLUTORES          */
/* ========================================================= */
export const getAllResolvers = (
    page: number = 0,
    size: number = 10
) => {
    return client.getAllResolvers(page, size);
};

/* ========================================================= */
/*           PAGINADO: RESOLUTORES POR MÓDULO                */
/* ========================================================= */
export const getResolversByModule = (
    moduleId: number,
    page: number = 0,
    size: number = 10
) => {
    return client.getResolversByModule(moduleId, page, size);
};

/* ========================================================= */
/*                        OTROS SERVICIOS                    */
/* ========================================================= */

export const getResolversByEmail = (email: string) =>
    client.getResolverDetails(email);

export const getResolverModules = (email: string) =>
    client.getResolverModules(email);

export const upsertModuleResolver = (body: ModuleResolver) =>
    client.upsertResolver(body);

export const deleteResolver = (id: number) =>
    client.deleteResolver(id);

export const getResolverById = (id: number) =>
    client.getResolverById(id);

/* ========================================================= */
/*                    CATÁLOGO DE MÓDULOS                    */
/* ========================================================= */
export const loadModulesCatalog = async () => {
    const api = ConfigurationBuilder.client;
    const arr = await api.getCatalog(3);

    return arr.map(({ id, description }) => ({
        value: String(id),
        label: description,
    }));
};

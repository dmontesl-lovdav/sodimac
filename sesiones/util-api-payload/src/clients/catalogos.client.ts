// src/clients/catalogos.client.ts
/**
 * Cliente HTTP para consumir catalogos-api
 * Similar a la implementacion de fiscal-api pero adaptado a TypeScript/Node.js
 */

import catalogosConfig from '../config/catalogos.config.js';

interface CatalogMessage {
    key: string;
    description: string;
    dict_id?: number;
}

interface CacheEntry {
    message: string;
    timestamp: number;
}

/**
 * Cliente para obtener mensajes desde catalogos-api con cache local
 */
class CatalogosClient {
    private cache: Map<string, CacheEntry> = new Map();

    /**
     * Obtiene un mensaje desde catalogos-api
     * @param key Clave del mensaje (ej: ERR100, BUS101)
     * @param langId ID del idioma (1=ES, 2=EN, 3=PT)
     * @returns Mensaje o null si no se encuentra
     */
    async getMessage(key: string, langId: number = catalogosConfig.defaultLangId): Promise<string | null> {
        if (!catalogosConfig.enabled) {
            return null;
        }

        const cacheKey = `${key}_${langId}`;

        // Verificar cache
        const cached = this.cache.get(cacheKey);
        if (cached && Date.now() - cached.timestamp < catalogosConfig.cacheTtl) {
            return cached.message;
        }

        try {
            const url = `${catalogosConfig.baseUrl}/message/${key}?lang=${langId}`;

            const controller = new AbortController();
            const timeoutId = setTimeout(() => controller.abort(), catalogosConfig.timeout);

            const response = await fetch(url, {
                method: 'GET',
                headers: {
                    'Content-Type': 'application/json',
                    'Accept': 'application/json',
                },
                signal: controller.signal,
            });

            clearTimeout(timeoutId);

            if (!response.ok) {
                console.warn(`[CatalogosClient] Error ${response.status} obteniendo mensaje ${key}`);
                return null;
            }

            const data: CatalogMessage = await response.json();

            if (data.description) {
                // Guardar en cache
                this.cache.set(cacheKey, {
                    message: data.description,
                    timestamp: Date.now(),
                });
                return data.description;
            }

            return null;
        } catch (error) {
            if (error instanceof Error) {
                if (error.name === 'AbortError') {
                    console.warn(`[CatalogosClient] Timeout obteniendo mensaje ${key}`);
                } else {
                    console.warn(`[CatalogosClient] Error obteniendo mensaje ${key}: ${error.message}`);
                }
            }
            return null;
        }
    }

    /**
     * Obtiene un mensaje formateado con parametros desde catalogos-api
     * @param key Clave del mensaje
     * @param langId ID del idioma
     * @param params Parametros para formatear
     * @returns Mensaje formateado o null
     */
    async getMessageFormatted(
        key: string,
        langId: number = catalogosConfig.defaultLangId,
        params: string[] = []
    ): Promise<string | null> {
        if (!catalogosConfig.enabled) {
            return null;
        }

        try {
            const paramsQuery = params.length > 0 ? `&params=${params.join(',')}` : '';
            const url = `${catalogosConfig.baseUrl}/message/${key}/format?lang=${langId}${paramsQuery}`;

            const controller = new AbortController();
            const timeoutId = setTimeout(() => controller.abort(), catalogosConfig.timeout);

            const response = await fetch(url, {
                method: 'GET',
                headers: {
                    'Content-Type': 'application/json',
                    'Accept': 'application/json',
                },
                signal: controller.signal,
            });

            clearTimeout(timeoutId);

            if (!response.ok) {
                return null;
            }

            const data: CatalogMessage = await response.json();
            return data.description || null;
        } catch (error) {
            console.warn(`[CatalogosClient] Error obteniendo mensaje formateado ${key}`);
            return null;
        }
    }

    /**
     * Limpia el cache de mensajes
     */
    clearCache(): void {
        this.cache.clear();
        console.log('[CatalogosClient] Cache limpiado');
    }

    /**
     * Obtiene el tamano actual del cache
     */
    getCacheSize(): number {
        return this.cache.size;
    }

    /**
     * Indica si catalogos-api esta habilitado
     */
    isEnabled(): boolean {
        return catalogosConfig.enabled;
    }
}

// Singleton
export const catalogosClient = new CatalogosClient();
export default catalogosClient;

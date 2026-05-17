// src/config/catalogos.config.ts
/**
 * Configuracion para el cliente de catalogos-api
 * Similar a la configuracion de fiscal-api en Java/Spring
 */

export interface CatalogosConfig {
    /** URL base de catalogos-api */
    baseUrl: string;
    /** Habilitar consulta a catalogos-api (false = usar fallback) */
    enabled: boolean;
    /** ID de idioma por defecto (1=ES, 2=EN, 3=PT) */
    defaultLangId: number;
    /** Timeout en milisegundos para peticiones HTTP */
    timeout: number;
    /** Tiempo de vida del cache en milisegundos */
    cacheTtl: number;
}

const config: CatalogosConfig = {
    baseUrl: process.env.CATALOGOS_API_URL || 'http://localhost:8081',
    enabled: process.env.CATALOGOS_API_ENABLED === 'true',
    defaultLangId: parseInt(process.env.CATALOGOS_API_LANG || '1', 10),
    timeout: parseInt(process.env.CATALOGOS_API_TIMEOUT || '5000', 10),
    cacheTtl: parseInt(process.env.CATALOGOS_API_CACHE_TTL || '300000', 10), // 5 minutos
};

export default config;

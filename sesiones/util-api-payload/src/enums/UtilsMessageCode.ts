// src/enums/UtilsMessageCode.ts
/**
 * Enumeracion de codigos de mensaje para utils-api
 * Similar a FiscalMessageCode en fiscal-api
 *
 * Convencion:
 * - ERR100+: Errores tecnicos
 * - BUS100+: Errores de negocio
 * - RES100+: Mensajes de exito
 * - INF100+: Mensajes informativos
 * - WRN100+: Advertencias
 */

export enum UtilsMessageCode {
    // ========== ERRORES TECNICOS (ERR100+) ==========
    /** Error de conexion con la base de datos */
    ERR100 = 'ERR100',
    /** Error al procesar el parametro {0} */
    ERR101 = 'ERR101',
    /** Error de validacion en los datos de entrada */
    ERR102 = 'ERR102',
    /** El recurso solicitado no fue encontrado */
    ERR103 = 'ERR103',
    /** Error interno del servidor */
    ERR104 = 'ERR104',
    /** Timeout de operacion */
    ERR105 = 'ERR105',
    /** Error de serializacion */
    ERR106 = 'ERR106',
    /** Error de configuracion */
    ERR107 = 'ERR107',

    // ========== ERRORES DE NEGOCIO (BUS100+) ==========
    /** Parametro duplicado */
    BUS100 = 'BUS100',
    /** Modulo no encontrado */
    BUS101 = 'BUS101',
    /** Parametro no encontrado */
    BUS102 = 'BUS102',
    /** Mensaje duplicado */
    BUS103 = 'BUS103',
    /** Mensaje no encontrado */
    BUS104 = 'BUS104',
    /** Proceso no encontrado */
    BUS105 = 'BUS105',
    /** Tipo de elemento no encontrado */
    BUS106 = 'BUS106',
    /** Elemento no encontrado */
    BUS107 = 'BUS107',
    /** Parametro inactivo */
    BUS108 = 'BUS108',
    /** Valor de parametro invalido */
    BUS109 = 'BUS109',
    /** Relacion mensaje-aplicativo duplicada */
    BUS110 = 'BUS110',
    /** Modulo en uso */
    BUS111 = 'BUS111',

    // ========== MENSAJES DE EXITO (RES100+) ==========
    /** Parametro creado exitosamente */
    RES100 = 'RES100',
    /** Parametro actualizado exitosamente */
    RES101 = 'RES101',
    /** Parametro eliminado exitosamente */
    RES102 = 'RES102',
    /** Modulo creado exitosamente */
    RES103 = 'RES103',
    /** Configuracion guardada exitosamente */
    RES104 = 'RES104',

    // ========== MENSAJES INFORMATIVOS (INF100+) ==========
    /** Sin resultados para la busqueda */
    INF100 = 'INF100',
    /** Operacion en proceso */
    INF101 = 'INF101',
    /** Cache actualizado */
    INF102 = 'INF102',

    // ========== ADVERTENCIAS (WRN100+) ==========
    /** Parametro proximo a expirar */
    WRN100 = 'WRN100',
    /** Version obsoleta */
    WRN101 = 'WRN101',
    /** Limite de registros excedido */
    WRN102 = 'WRN102',
}

/**
 * Mensajes fallback en espanol (usados cuando catalogos-api no esta disponible)
 */
export const UtilsMessageFallback: Record<UtilsMessageCode, string> = {
    // Errores tecnicos
    [UtilsMessageCode.ERR100]: 'Error de conexion con la base de datos',
    [UtilsMessageCode.ERR101]: 'Error al procesar el parametro {0}',
    [UtilsMessageCode.ERR102]: 'Error de validacion en los datos de entrada',
    [UtilsMessageCode.ERR103]: 'El recurso solicitado no fue encontrado',
    [UtilsMessageCode.ERR104]: 'Error interno del servidor. Contacte al administrador',
    [UtilsMessageCode.ERR105]: 'Tiempo de espera agotado para la operacion',
    [UtilsMessageCode.ERR106]: 'Error al serializar/deserializar datos',
    [UtilsMessageCode.ERR107]: 'Error en la configuracion del sistema',

    // Errores de negocio
    [UtilsMessageCode.BUS100]: 'Ya existe un parametro con el nombre {0} y version {1}',
    [UtilsMessageCode.BUS101]: 'El modulo con ID {0} no existe',
    [UtilsMessageCode.BUS102]: 'El parametro con ID {0} no existe',
    [UtilsMessageCode.BUS103]: 'Ya existe un mensaje con el codigo {0}',
    [UtilsMessageCode.BUS104]: 'El mensaje con codigo {0} no existe',
    [UtilsMessageCode.BUS105]: 'El proceso con ID {0} no existe',
    [UtilsMessageCode.BUS106]: 'El tipo de elemento con ID {0} no existe',
    [UtilsMessageCode.BUS107]: 'El elemento con ID {0} no existe',
    [UtilsMessageCode.BUS108]: 'El parametro {0} se encuentra inactivo',
    [UtilsMessageCode.BUS109]: 'El valor del parametro {0} no es valido: {1}',
    [UtilsMessageCode.BUS110]: 'La relacion mensaje {0} - aplicativo {1} ya existe',
    [UtilsMessageCode.BUS111]: 'El modulo {0} no puede eliminarse porque tiene parametros asociados',

    // Mensajes de exito
    [UtilsMessageCode.RES100]: 'Parametro {0} creado exitosamente',
    [UtilsMessageCode.RES101]: 'Parametro {0} actualizado exitosamente',
    [UtilsMessageCode.RES102]: 'Parametro eliminado exitosamente',
    [UtilsMessageCode.RES103]: 'Modulo {0} creado exitosamente',
    [UtilsMessageCode.RES104]: 'Configuracion guardada exitosamente',

    // Mensajes informativos
    [UtilsMessageCode.INF100]: 'No se encontraron resultados para la busqueda',
    [UtilsMessageCode.INF101]: 'La operacion se esta procesando',
    [UtilsMessageCode.INF102]: 'Cache de parametros actualizado',

    // Advertencias
    [UtilsMessageCode.WRN100]: 'El parametro {0} expira en {1} dias',
    [UtilsMessageCode.WRN101]: 'La version {0} del parametro {1} esta obsoleta',
    [UtilsMessageCode.WRN102]: 'Se excedio el limite de {0} registros. Mostrando primeros resultados',
};

/**
 * HTTP Status asociado a cada tipo de codigo
 */
export function getHttpStatus(code: UtilsMessageCode): number {
    if (code.startsWith('ERR')) {
        // ERR103 es not found
        if (code === UtilsMessageCode.ERR103) return 404;
        // ERR102 es validacion
        if (code === UtilsMessageCode.ERR102) return 400;
        // Otros errores tecnicos
        return 500;
    }
    if (code.startsWith('BUS')) {
        // Errores de negocio "no encontrado"
        if ([UtilsMessageCode.BUS101, UtilsMessageCode.BUS102, UtilsMessageCode.BUS104,
             UtilsMessageCode.BUS105, UtilsMessageCode.BUS106, UtilsMessageCode.BUS107].includes(code)) {
            return 404;
        }
        // Errores de negocio "duplicado"
        if ([UtilsMessageCode.BUS100, UtilsMessageCode.BUS103, UtilsMessageCode.BUS110].includes(code)) {
            return 409;
        }
        // Otros errores de negocio
        return 400;
    }
    if (code.startsWith('RES')) return 200;
    if (code.startsWith('INF')) return 200;
    if (code.startsWith('WRN')) return 200;
    return 500;
}

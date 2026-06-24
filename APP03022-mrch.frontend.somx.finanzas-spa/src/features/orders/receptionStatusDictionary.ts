/**
 * CatEstatusRecepcion — mapeo estático (sin API).
 * Colores según insumo FBC: Amarillo → warning, Verde → success, Rojo → error.
 */
export type ReceptionStatusDictEntry = {
    shortLabel: string;
    description: string;
    pillType: 'success' | 'warning' | 'error' | 'info';
};

export const RECEPTION_STATUS_DICTIONARY: Record<
    number,
    ReceptionStatusDictEntry
> = {
    0: {
        shortLabel: 'Disponible',
        description: 'Recepción disponible',
        pillType: 'warning',
    },
    1: {
        shortLabel: 'Consumida',
        description: 'Recepción consumida (Relación con factura)',
        pillType: 'success',
    },
    2: {
        shortLabel: 'Consumida manual',
        description: 'Recepción consumida manual',
        pillType: 'success',
    },
    3: {
        shortLabel: 'En proceso contable',
        description: 'En proceso de contabilizar factura',
        pillType: 'warning',
    },
    4: {
        shortLabel: 'Rechazo contable',
        description: 'Factura rechazada',
        pillType: 'error',
    },
    5: {
        shortLabel: 'En proceso de pago',
        description: 'Factura en proceso de pago',
        pillType: 'warning',
    },
    6: {
        shortLabel: 'Pagada',
        description: 'Recepción pagada',
        pillType: 'success',
    },
    7: {
        shortLabel: 'Cancelada',
        description: 'Recepción cancelada',
        pillType: 'warning',
    },
    8: {
        shortLabel: 'Borrado',
        description: 'Recepción borrada',
        pillType: 'error',
    },
    9: {
        shortLabel: 'En validación',
        description:
            'Recepción en proceso de validación por parte del equipo de negocio',
        pillType: 'warning',
    },
};

const STATUS_IDS = Object.keys(RECEPTION_STATUS_DICTIONARY)
    .map(Number)
    .sort((a, b) => a - b);

/** Resuelve texto largo + tipo de pill para grid / exportación. */
export function resolveReceptionStatusFromDictionary(
    status: number
): { label: string; pillType: string } {
    const sid = Number(status);
    const entry = RECEPTION_STATUS_DICTIONARY[sid];
    if (entry) {
        return { label: entry.shortLabel, pillType: entry.pillType };
    }
    return { label: 'Desconocido', pillType: 'error' };
}

/** Lista ordenada de ids definidos en el diccionario. */
export function receptionStatusDefinedIds(): number[] {
    return STATUS_IDS.slice();
}

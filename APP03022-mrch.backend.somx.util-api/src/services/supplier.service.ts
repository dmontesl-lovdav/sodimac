import * as supplierRepo from '@/repositories/supplier.repo.js';
import type { SupplierFilters } from '@/repositories/supplier.repo.js';

const WRN7029 = {
    success: false,
    code: 'WRN7029',
    message: 'El usuario no tiene configurado los atributos para el manejo de información, favor de validar con el administrador',
};

/**
 * Parsea el header x-user-types en la lista de ATR002 keys.
 * null  → sin restricción (header ausente o admin)
 * []    → WRN7029 (header presente pero vacío)
 * [...] → filtro activo
 */
function parseTypesHeader(header: string | undefined): string[] | null | 'wrn7029' {
    if (header === undefined || header === null) return null;
    const trimmed = header.trim();
    if (trimmed === '') return 'wrn7029';
    if (trimmed === '-1') return null;
    return trimmed.split(',').map(t => t.trim()).filter(Boolean);
}

export async function listSuppliers(
    filters: SupplierFilters,
    xUserTypes?: string,
): Promise<{ success: boolean; data?: unknown; code?: string; message?: string }> {
    const allowedTypes = parseTypesHeader(xUserTypes);
    if (allowedTypes === 'wrn7029') return WRN7029;

    const result = await supplierRepo.findSuppliers(filters, allowedTypes);

    return {
        success: true,
        data: {
            items: result.items,
            total: result.total,
            page: result.page,
            pageSize: result.pageSize,
            totalPages: Math.ceil(result.total / result.pageSize),
        },
    };
}

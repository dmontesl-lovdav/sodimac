import { z } from 'zod';

export interface ConversionDto {
    idConversion: number;
    idElementoOrigen?: number | null;
    elementoOrigen?: string | null;
    valorElementoOrigen?: string | null;
    estatusElementoOrigen?: string | null;
    catalogoElementoOrigen?: string | null;
    idCatalogoElementoOrigen?: number | null;
    idElemento?: number | null;
    elemento?: string | null;
    valor?: string | null;
    catalogoOrigen?: string | null;
    fechaInicioVigencia?: string | null;
    fechaFinVigencia?: string | null;
    estatus?: string | null;
    esPrincipal?: boolean | null;
    idUsuarioRegistro?: string | null;
    fechaRegistro?: Date | null;
    idUsuarioActualizacion?: string | null;
    fechaActualizacion?: Date | null;
}

export interface ConversionPageResponse {
    items: ConversionDto[];
    page: number;
    pageSize: number;
    total: number;
    totalPages: number;
}

export const ConversionCreateSchema = z.object({
    sourceElementId: z.number({ message: 'sourceElementId es requerido' }).int(),
    targetElementId: z.number({ message: 'targetElementId es requerido' }).int(),
    validFrom: z.string().optional().nullable(),
    validTo: z.string().optional().nullable(),
    isPrincipal: z.boolean().optional().nullable()
});
export type ConversionCreateDto = z.infer<typeof ConversionCreateSchema>;

export const ConversionUpdateSchema = z.object({
    targetElementId: z.number({ message: 'El ID del elemento destino es obligatorio' }).int(),
    validFrom: z.string().optional().nullable(),
    validTo: z.string().optional().nullable(),
    status: z.number().int().optional().nullable()
});
export type ConversionUpdateDto = z.infer<typeof ConversionUpdateSchema>;


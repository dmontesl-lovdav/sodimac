import { z } from 'zod';

export interface CatalogDetailDto {
    id?: number;
    key: string | null;
    internalStatus?: number | null;
    externalKey?: string | null;
    value?: string | null;
    description?: string | null;
    color?: string | null;
    sortOrder?: number | null;
    validFrom?: string | null;
    validTo?: string | null;
    parentElementId?: number | null;
    attributes?: any;
}

export interface CatalogHeaderDto {
    code: string;
    prefix: string;
    name: string;
    description?: string | null;
    module?: string | null;
    catalogType?: string | null;
    details?: CatalogDetailDto[];
}

export interface CatalogSimpleDto {
    id: number;
    code: string;
    name: string;
    description?: string | null;
    catalogType?: string | null;
    status: number;
}

export interface CatalogResponseDto {
    id: number;
    code: string;
    prefix: string;
    name: string;
    description?: string | null;
    module?: string | null;
    catalogType?: string | null;
    status: number;
    createdBy?: string | null;
    createdAt?: Date | null;
    updatedBy?: string | null;
    updatedAt?: Date | null;
    elementCount?: number;
}

export interface CatalogPageResponse {
    items: CatalogResponseDto[];
    page: number;
    pageSize: number;
    total: number;
    totalPages: number;
    hasNext: boolean;
    hasPrevious: boolean;
}

export const CatalogCreateSchema = z.object({
    code: z.string().max(64, 'El código no debe exceder 64 caracteres').optional(),
    prefix: z.string().max(3, 'El prefijo no debe exceder 3 caracteres').optional(),
    name: z.string().min(1, 'El nombre del catálogo es obligatorio').max(128, 'El nombre no debe exceder 128 caracteres'),
    description: z.string().max(250, 'La descripción no debe exceder 250 caracteres').optional(),
    catalogType: z.string().min(1, 'El tipo de catálogo es obligatorio'),
    module: z.string().optional()
});
export type CatalogCreateDto = z.infer<typeof CatalogCreateSchema>;

export const CatalogUpdateSchema = z.object({
    name: z.string().max(128, 'El nombre no debe exceder 128 caracteres').optional(),
    description: z.string().max(250, 'La descripción no debe exceder 250 caracteres').optional(),
    catalogType: z.string().optional(),
    status: z.number().int().optional(),
    module: z.string().optional()
});
export type CatalogUpdateDto = z.infer<typeof CatalogUpdateSchema>;

export interface CatalogElementDto {
    id: number;
    catalogId?: number | null;
    catalogCode?: string | null;
    element?: string | null;
    value?: string | null;
    key?: string | null;
    validFrom?: string | null;
    validTo?: string | null;
    status?: number | null;
    statusDescription?: string | null;
    parentCatalogId?: number | null;
    parentCatalogName?: string | null;
    parentElementId?: number | null;
    parentElementName?: string | null;
    createdBy?: string | null;
    createdAt?: Date | null;
    updatedBy?: string | null;
    updatedAt?: Date | null;
    externalKey?: string | null;
    sortOrder?: number | null;
    attributes?: any;
}

export interface CatalogElementPageResponse {
    items: CatalogElementDto[];
    page: number;
    pageSize: number;
    total: number;
    totalPages: number;
    hasNext: boolean;
    hasPrevious: boolean;
}

const extKeyPattern = /^[a-zA-Z0-9._-]*$/;

export const CatalogElementCreateSchema = z.object({
    element: z
        .string()
        .min(1, 'El nombre del elemento es obligatorio')
        .max(100, 'El nombre del elemento no puede exceder 100 caracteres'),
    value: z
        .string()
        .max(100, 'El valor del elemento no puede exceder 100 caracteres')
        .optional()
        .nullable(),
    validFrom: z.string().min(1, 'La fecha de inicio de vigencia es obligatoria'),
    validTo: z.string().optional().nullable(),
    parentCatalogId: z.number().int().optional().nullable(),
    parentElementId: z.number().int().optional().nullable(),
    externalKey: z
        .string()
        .max(50, 'El valor de conversión no puede exceder 50 caracteres')
        .regex(extKeyPattern, 'El valor de conversión solo puede contener letras, números, guiones, guiones bajos y puntos')
        .optional()
        .nullable(),
    sortOrder: z.number().int().optional().nullable(),
    attributes: z.any().optional().nullable()
});
export type CatalogElementCreateDto = z.infer<typeof CatalogElementCreateSchema>;

export const CatalogElementUpdateSchema = z.object({
    element: z.string().max(100, 'El nombre del elemento no puede exceder 100 caracteres').optional().nullable(),
    value: z.string().max(100, 'El valor del elemento no puede exceder 100 caracteres').optional().nullable(),
    validFrom: z.string().optional().nullable(),
    validTo: z.string().optional().nullable(),
    parentCatalogId: z.number().int().optional().nullable(),
    parentElementId: z.number().int().optional().nullable(),
    externalKey: z
        .string()
        .max(50, 'El valor de conversión no puede exceder 50 caracteres')
        .regex(extKeyPattern, 'El valor de conversión solo puede contener letras, números, guiones, guiones bajos y puntos')
        .optional()
        .nullable(),
    sortOrder: z.number().int().optional().nullable(),
    attributes: z.any().optional().nullable(),
    status: z.number().int().optional().nullable()
});
export type CatalogElementUpdateDto = z.infer<typeof CatalogElementUpdateSchema>;

export const CatalogElementStatusSchema = z.object({
    status: z.number({ message: 'El estatus es obligatorio' }).int()
});
export type CatalogElementStatusDto = z.infer<typeof CatalogElementStatusSchema>;


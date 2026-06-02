import { z } from 'zod';

export interface SupplierTypeDto {
    id: number;
    code: string;
    description: string;
}

export interface PaymentConditionDto {
    id: number;
    conditionName: string;
    days: number;
}

export interface SupplierDto {
    id: number;
    supplierNumber: string;
    rfc: string;
    businessName: string;
    supplierType?: SupplierTypeDto | null;
    logo?: string | null;
    paymentCondition?: PaymentConditionDto | null;
    emailPrincipal?: string | null;
    emailFinancial?: string | null;
    emailCommercial?: string | null;
    status: number;
}

export interface SupplierBlockInfoDto {
    validFrom: string;
    validTo: string;
    blockReason?: string | null;
}

export interface SupplierFilterDto {
    id: number;
    supplierNumber: string;
    rfc: string;
    businessName: string;
    supplierType?: SupplierTypeDto | null;
    paymentCondition?: PaymentConditionDto | null;
    status: number;
    blocked: boolean;
    blockInfo?: SupplierBlockInfoDto | null;
}

const emailRequired = (label: string) => z
    .string()
    .trim()
    .min(1, `${label} es requerido`)
    .max(255, `${label} no puede exceder 255 caracteres`)
    .email(`${label} no tiene un formato válido`);

const emailOptional = (label: string) => z
    .string()
    .trim()
    .max(255, `${label} no puede exceder 255 caracteres`)
    .email(`${label} no tiene un formato válido`)
    .optional()
    .nullable()
    .or(z.literal('').transform(() => null));

export const SupplierCreateSchema = z.object({
    supplierNumber: z
        .string()
        .min(1, 'El numero de proveedor es requerido')
        .max(20, 'El numero de proveedor no puede exceder 20 caracteres'),
    rfc: z
        .string()
        .min(12, 'El RFC debe tener entre 12 y 13 caracteres')
        .max(13, 'El RFC debe tener entre 12 y 13 caracteres'),
    businessName: z
        .string()
        .min(1, 'La razon social es requerida')
        .max(255, 'La razon social no puede exceder 255 caracteres'),
    supplierTypeId: z.number().int().optional().nullable(),
    logo: z.string().max(500, 'La URL del logo no puede exceder 500 caracteres').optional().nullable(),
    paymentConditionId: z.number().int().optional().nullable(),
    emailPrincipal: emailRequired('El Email Principal'),
    emailFinancial: emailRequired('El Email CXC'),
    emailCommercial: emailOptional('El Email Comercial')
});
export type SupplierCreateDto = z.infer<typeof SupplierCreateSchema>;

export const SupplierUpdateSchema = z.object({
    rfc: z
        .string()
        .min(12, 'El RFC debe tener entre 12 y 13 caracteres')
        .max(13, 'El RFC debe tener entre 12 y 13 caracteres')
        .optional()
        .nullable(),
    businessName: z.string().max(255, 'La razon social no puede exceder 255 caracteres').optional().nullable(),
    supplierTypeId: z.number().int().optional().nullable(),
    logo: z.string().max(500, 'La URL del logo no puede exceder 500 caracteres').optional().nullable(),
    paymentConditionId: z.number().int().optional().nullable(),
    emailPrincipal: emailRequired('El Email Principal').optional(),
    emailFinancial: emailRequired('El Email CXC').optional(),
    emailCommercial: emailOptional('El Email Comercial'),
    status: z.number().int().optional().nullable()
});
export type SupplierUpdateDto = z.infer<typeof SupplierUpdateSchema>;

export interface SupplierBlockDto {
    id: number;
    supplierNumber: string;
    validFrom: string;
    validTo: string;
    blockReason?: string | null;
    status: number;
    currentlyBlocked?: boolean;
    createdAt?: Date | null;
    createdBy?: string | null;
    updatedAt?: Date | null;
    updatedBy?: string | null;
}

export interface SupplierBlockResponseDto {
    message: string;
    data: SupplierBlockDto;
}

export const SupplierBlockCreateSchema = z.object({
    supplierNumber: z
        .string()
        .min(1, 'El numero de proveedor es requerido')
        .max(20, 'El numero de proveedor no puede exceder 20 caracteres'),
    validFrom: z.string().min(1, 'La fecha de inicio es requerida'),
    validTo: z.string().min(1, 'La fecha de fin es requerida'),
    blockReason: z.string().max(255, 'La razon del bloqueo no puede exceder 255 caracteres').optional().nullable()
});
export type SupplierBlockCreateDto = z.infer<typeof SupplierBlockCreateSchema>;

export const SupplierBlockUpdateSchema = z.object({
    validFrom: z.string().optional().nullable(),
    validTo: z.string().optional().nullable(),
    blockReason: z.string().max(255, 'La razon del bloqueo no puede exceder 255 caracteres').optional().nullable(),
    status: z.number().int().optional().nullable()
});
export type SupplierBlockUpdateDto = z.infer<typeof SupplierBlockUpdateSchema>;


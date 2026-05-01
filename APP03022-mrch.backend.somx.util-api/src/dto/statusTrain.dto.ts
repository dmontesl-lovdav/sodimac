import { z } from 'zod';

export interface StatusTrainDto {
    id: number;
    optionId: number;
    sourceStatus: number;
    targetStatus: number;
    createdBy?: number | null;
    createdAt?: Date | null;
    updatedBy?: number | null;
    updatedAt?: Date | null;
}

export interface StatusTrainListResponse {
    success: boolean;
    code?: string;
    message?: string;
    count?: number;
    data?: StatusTrainDto[];
}

export interface StatusTrainValidationResponse {
    success: boolean;
    valid: boolean;
    code?: string;
    message?: string;
    data?: StatusTrainDto;
}

export function statusTrainListSuccess(data: StatusTrainDto[]): StatusTrainListResponse {
    return {
        success: true,
        count: data?.length ?? 0,
        data
    };
}

export function statusTrainListSourceNotFound(): StatusTrainListResponse {
    return {
        success: false,
        code: 'WRN7010',
        message: 'El estatus origen no existe catalogado. Por favor, valide la información antes de continuar.'
    };
}

export function statusTrainValidationSuccess(data: StatusTrainDto): StatusTrainValidationResponse {
    return {
        success: true,
        valid: true,
        data
    };
}

export function statusTrainSourceNotFound(): StatusTrainValidationResponse {
    return {
        success: false,
        valid: false,
        code: 'WRN7010',
        message: 'El estatus origen no existe catalogado. Por favor, valide la información antes de continuar.'
    };
}

export function statusTrainTransitionNotAllowed(): StatusTrainValidationResponse {
    return {
        success: false,
        valid: false,
        code: 'WRN7011',
        message: 'El estatus destino no existe catalogado. Por favor, valide la información antes de continuar.'
    };
}

export const StatusTrainCreateSchema = z.object({
    optionId: z.number({ message: 'El optionId es requerido' }).int(),
    sourceStatus: z.number({ message: 'El sourceStatus es requerido' }).int(),
    targetStatus: z.number({ message: 'El targetStatus es requerido' }).int(),
    createdBy: z.number({ message: 'El createdBy es requerido' })
});
export type StatusTrainCreateDto = z.infer<typeof StatusTrainCreateSchema>;

export const StatusTrainUpdateSchema = z.object({
    sourceStatus: z.number({ message: 'El sourceStatus es requerido' }).int(),
    targetStatus: z.number({ message: 'El targetStatus es requerido' }).int(),
    updatedBy: z.number({ message: 'El updatedBy es requerido' })
});
export type StatusTrainUpdateDto = z.infer<typeof StatusTrainUpdateSchema>;


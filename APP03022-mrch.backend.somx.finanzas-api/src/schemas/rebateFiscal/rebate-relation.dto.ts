/**
 * ============================================================================
 * DTOs: Rebate Relation (STM-973)
 * ============================================================================
 * Data Transfer Objects para relacionar descuento comercial con NC
 */

/**
 * Request DTO para STM-973
 */
export interface RebateRelationRequestDto {
    /** UUID de la factura original (no de la NC) */
    uuid: string;
    /** Número del documento de rebate */
    numeroDocumento: string;
    /** Referencia del documento */
    referenciaDocumento: string;
    /** Número del proveedor */
    numeroProveedor: string;
    /** ID del usuario que realiza la operación */
    usuario: string;
}

/**
 * Response DTO para STM-973
 */
export interface RebateRelationResponseDto {
    /** Indica si la operación fue exitosa */
    success: boolean;
    /** Mensaje descriptivo del resultado */
    message: string;
    /** Código de negocio para identificación del resultado */
    businessCode: string;
    /** UUID del StampedRebate creado (solo si success=true) */
    stampedRebateUuid?: string;
    /** UUID del Rebate creado (solo si success=true) */
    rebateUuid?: string;
}

/**
 * ============================================================================
 * DTOs: Rebate Search (STM-875)
 * ============================================================================
 * Data Transfer Objects para búsqueda de descuentos comerciales
 */

/**
 * Request DTO para STM-875
 */
export interface RebateSearchRequestDto {
    /** Número de proveedor (OBLIGATORIO) */
    idProveedor: string;
    /** Número de documento (opcional) */
    numeroDocumento?: string;
    /** Referencia del documento (opcional) */
    referenciaDocumento?: string;
    /** UUID fiscal de la factura relacionada (opcional) */
    uuid?: string;
    /** Número de página (default: 0) */
    page?: number;
    /** Tamaño de página (default: 20) */
    size?: number;
}

/**
 * Response DTO individual para STM-875
 */
export interface RebateSearchResponseDto {
    /** UUID del rebate */
    rebateUuid: string;
    /** Número del documento */
    documentNumber: string;
    /** Número de referencia */
    referenceNumber: string;
    /** Documento SAP asociado */
    sapDocument: string;
    /** Número de proveedor */
    vendorNumber: number;
    /** Monto del rebate */
    amount: number;
    /** UUID fiscal de la factura relacionada (si existe) */
    invoiceFiscalUuid?: string;
    /** Estado del rebate (0=Inactivo, 1=Activo, 2=Procesado, 3=Cancelado) */
    status: number;
    /** Nombre del estado */
    statusName: string;
    /** ID del usuario que creó el registro */
    createdBy?: number;
    /** Fecha de creación */
    createdAt: Date;
    /** ID del usuario que actualizó el registro */
    updatedBy?: number;
    /** Fecha de actualización */
    updatedAt?: Date;
}

/**
 * Response DTO paginado para STM-875
 */
export interface RebateSearchPageResponseDto {
    /** Lista de rebates encontrados */
    data: RebateSearchResponseDto[];
    /** Total de registros encontrados */
    total: number;
    /** Página actual */
    page: number;
    /** Tamaño de página */
    size: number;
    /** Total de páginas */
    totalPages: number;
}

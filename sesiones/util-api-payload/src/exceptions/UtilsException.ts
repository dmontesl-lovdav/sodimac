// src/exceptions/UtilsException.ts
/**
 * Excepcion personalizada para utils-api
 * Similar a FiscalException en fiscal-api
 */

import { UtilsMessageCode, getHttpStatus } from '../enums/UtilsMessageCode.js';

export class UtilsException extends Error {
    public readonly code: UtilsMessageCode;
    public readonly httpStatus: number;
    public readonly additionalInfo?: string;
    public readonly originalError?: Error;

    constructor(
        code: UtilsMessageCode,
        message: string,
        additionalInfo?: string,
        originalError?: Error
    ) {
        super(message);
        this.name = 'UtilsException';
        this.code = code;
        this.httpStatus = getHttpStatus(code);
        if (additionalInfo !== undefined) this.additionalInfo = additionalInfo;
        if (originalError !== undefined) this.originalError = originalError;

        // Mantener el stack trace correcto en V8
        if (Error.captureStackTrace) {
            Error.captureStackTrace(this, UtilsException);
        }
    }

    /**
     * Convierte la excepcion a un objeto para respuesta JSON
     */
    toJSON(): Record<string, unknown> {
        return {
            success: false,
            code: this.code,
            message: this.message,
            additionalInfo: this.additionalInfo,
            status: this.httpStatus,
        };
    }

    /**
     * Verifica si es un error de negocio (BUS)
     */
    isBusinessError(): boolean {
        return this.code.startsWith('BUS');
    }

    /**
     * Verifica si es un error tecnico (ERR)
     */
    isTechnicalError(): boolean {
        return this.code.startsWith('ERR');
    }
}

export default UtilsException;

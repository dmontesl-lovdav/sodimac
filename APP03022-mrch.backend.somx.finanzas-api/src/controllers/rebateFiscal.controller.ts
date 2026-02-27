/**
 * ============================================================================
 * CONTROLLER: rebateFiscal.controller
 * ============================================================================
 * Controller Express para gestión de rebates con integración fiscal
 *
 * ENDPOINTS:
 * - POST /rebates/relate: STM-973 - Relacionar descuento con NC
 * - POST /rebates/search-v2: STM-875 - Buscar descuentos comerciales
 *
 * PATRÓN:
 * - Express tradicional (req, res, next)
 * - Uso de multer para archivos XML
 * - Validación con Zod schemas
 *
 * @author Sodimac Tech Team
 * @version 2.0 (Express)
 * @since 2025
 */

import type { Request, Response, NextFunction } from "express";
import multer from "multer";
import * as svc from "@/services/rebateFiscal.service.js";
import { HttpError } from "@/utils/HttpError.js";
import type {
    RebateRelationRequestDto
} from "@/schemas/rebateFiscal/rebate-relation.dto.js";
import type {
    RebateSearchRequestDto
} from "@/schemas/rebateFiscal/rebate-search.dto.js";

// Configurar multer para recibir archivos en memoria
const upload = multer({ storage: multer.memoryStorage() });

/**
 * POST /rebates/relate
 * STM-973: Relacionar Descuento Comercial con NC
 *
 * BODY (multipart/form-data):
 * - uuid: UUID de la factura original
 * - numeroDocumento: Número del documento de rebate
 * - referenciaDocumento: Referencia del documento
 * - numeroProveedor: Número del proveedor
 * - usuario: ID del usuario que realiza la operación
 * - xmlFile: Archivo XML de la NC (file upload)
 *
 * RESPONSE:
 * {
 *   "success": boolean,
 *   "message": string,
 *   "businessCode": string,
 *   "stampedRebateUuid"?: string,
 *   "rebateUuid"?: string
 * }
 */
export const relateRebate = [
    upload.single('xmlFile'),
    async (req: Request, res: Response, next: NextFunction) => {
        try {
            console.log('[relateRebate] Request recibido');
            console.log('[relateRebate] Body:', req.body);
            console.log('[relateRebate] File:', req.file ? 'Presente' : 'Ausente');

            // Validar archivo XML
            if (!req.file) {
                throw new HttpError(400, 'El archivo XML de NC es obligatorio');
            }

            // Mapear request a DTO
            const request: RebateRelationRequestDto = {
                uuid: req.body.uuid,
                numeroDocumento: req.body.numeroDocumento,
                referenciaDocumento: req.body.referenciaDocumento,
                numeroProveedor: req.body.numeroProveedor,
                usuario: req.body.usuario
            };

            // Validar campos obligatorios
            if (!request.uuid || !request.numeroDocumento || !request.referenciaDocumento ||
                !request.numeroProveedor || !request.usuario) {
                throw new HttpError(
                    400,
                    'Faltan campos obligatorios: uuid, numeroDocumento, referenciaDocumento, numeroProveedor, usuario'
                );
            }

            // Llamar al servicio
            const xmlBuffer = req.file.buffer;
            const response = await svc.relateRebate(request, xmlBuffer);

            // Retornar respuesta
            const statusCode = response.success ? 200 : 400;
            res.status(statusCode).json(response);

        } catch (error) {
            console.error('[relateRebate] Error:', error);
            next(error);
        }
    }
];

/**
 * POST /rebates/search-v2
 * STM-875: Buscar Descuentos Comerciales
 *
 * BODY (application/json):
 * {
 *   "idProveedor": string (OBLIGATORIO),
 *   "numeroDocumento"?: string,
 *   "referenciaDocumento"?: string,
 *   "uuid"?: string,
 *   "page"?: number,
 *   "size"?: number
 * }
 *
 * RESPONSE:
 * {
 *   "data": RebateSearchResponseDto[],
 *   "total": number,
 *   "page": number,
 *   "size": number,
 *   "totalPages": number
 * }
 */
export async function searchRebates(
    req: Request,
    res: Response,
    next: NextFunction
) {
    try {
        console.log('[searchRebates] Request recibido');
        console.log('[searchRebates] Body:', req.body);

        // Mapear request a DTO
        const searchRequest: RebateSearchRequestDto = {
            idProveedor: req.body.idProveedor,
            numeroDocumento: req.body.numeroDocumento,
            referenciaDocumento: req.body.referenciaDocumento,
            uuid: req.body.uuid,
            page: req.body.page || 0,
            size: req.body.size || 20
        };

        // Validar campo obligatorio
        if (!searchRequest.idProveedor) {
            throw new HttpError(
                400,
                'El campo idProveedor es obligatorio'
            );
        }

        // Llamar al servicio
        const response = await svc.searchRebates(searchRequest);

        // Retornar respuesta
        res.json(response);

    } catch (error) {
        console.error('[searchRebates] Error:', error);
        next(error);
    }
}

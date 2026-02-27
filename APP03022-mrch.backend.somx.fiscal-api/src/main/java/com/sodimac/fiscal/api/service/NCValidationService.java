package com.sodimac.fiscal.api.service;

import com.sodimac.fiscal.api.model.dto.NCValidationRequest;
import com.sodimac.fiscal.api.model.dto.NCValidationResponse;
import org.springframework.web.multipart.MultipartFile;

/**
 * ============================================================================
 * SERVICE: NCValidationService
 * ============================================================================
 * Servicio para validar relaciones NC-Factura en XMLs CFDI.
 *
 * RESPONSABILIDAD:
 * - Validar XML de Nota de Crédito
 * - Verificar que NC está relacionada con factura específica
 * - Extraer UUIDs y tipo de relación del XML
 * - NO GUARDAR NADA (solo validación)
 *
 * USADO POR:
 * - finanzas-api (para validar antes de guardar rebates)
 *
 * @author Sodimac Tech Team
 * @version 1.0
 * @since 2025
 */
public interface NCValidationService {

    /**
     * Valida que un XML de Nota de Crédito está relacionado con una factura.
     *
     * @param request Contiene el UUID fiscal de la factura
     * @param xmlFile Archivo XML de la Nota de Crédito
     * @return NCValidationResponse con resultado de validación
     */
    NCValidationResponse validateNCRelation(NCValidationRequest request, MultipartFile xmlFile);
}

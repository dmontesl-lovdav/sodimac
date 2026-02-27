package com.sodimac.fiscal.api.service;

import com.sodimac.fiscal.api.model.dto.InvoiceRegistrationResponse;
import org.springframework.web.multipart.MultipartFile;

/**
 * Servicio para el registro de facturas (I) y notas de crédito (E).
 *
 * Implementa las validaciones de negocio definidas en STM-337:
 * - Validación de RFC receptor autorizado
 * - Validación de versión CFDI vigente
 * - Validación de estructura y contenido de addenda
 * - Validación con SAT mediante PAC
 * - Registro en base de datos
 *
 * @author Sodimac Tech Team
 * @since 2025-11-10
 */
public interface InvoiceRegistrationService {

    /**
     * Registra una factura o nota de crédito en el sistema.
     *
     * @param xmlFile Archivo XML del CFDI
     * @return Respuesta del registro con código de negocio (BUS1xxx o BUS2xxx)
     */
    InvoiceRegistrationResponse registerInvoice(MultipartFile xmlFile);
}

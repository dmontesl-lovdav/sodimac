package com.sodimac.fiscal.api.service;

import com.sodimac.fiscal.api.model.dto.invoicexml.InvoiceXmlDto;

/**
 * Servicio para validar la estructura y contenido de addendas en facturas y notas de crédito.
 *
 * Valida que las addendas cumplan con los requisitos de Sodimac:
 * - Addenda_Sodimac para documentos de mercancía, servicios y transporte local
 * - Addenda_Sodimac_CartaPorte para transporte foráneo
 * - Campos obligatorios: RFC, UUID, Folio, NoOC, Proveedor
 *
 * @author Sodimac Tech Team
 * @since 2025-11-10
 */
public interface AddendaValidationService {

    /**
     * Valida la estructura y contenido de la addenda en un XML de factura/NC.
     *
     * @param xmlContent Contenido XML completo del CFDI
     * @param invoiceDto DTO procesado de la factura/NC
     * @return true si tiene addenda válida, false si no tiene addenda
     * @throws com.sodimac.fiscal.api.exception.FiscalException si la addenda existe pero es inválida (BUS2xxx)
     */
    boolean validateAddenda(String xmlContent, InvoiceXmlDto invoiceDto);
}

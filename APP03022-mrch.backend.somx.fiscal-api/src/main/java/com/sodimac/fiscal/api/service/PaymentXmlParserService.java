package com.sodimac.fiscal.api.service;

import com.sodimac.fiscal.api.model.dto.ParsedPaymentXmlDto;
import org.w3c.dom.Document;

/**
 * Servicio para parsear XMLs de complementos de pago.
 *
 * @author Sodimac Tech Team
 * @version 1.0
 * @since 2025
 */
public interface PaymentXmlParserService {

    /**
     * Parsea el contenido XML de un complemento de pago.
     *
     * @param xmlContent Contenido XML como String
     * @return DTO con todos los datos parseados
     * @throws RuntimeException si hay error en el parseo
     */
    ParsedPaymentXmlDto parsePaymentXml(String xmlContent);

    /**
     * Parsea el contenido XML de un complemento de pago desde un Document.
     *
     * @param document Documento XML
     * @return DTO con todos los datos parseados
     * @throws RuntimeException si hay error en el parseo
     */
    ParsedPaymentXmlDto parsePaymentXml(Document document);

    /**
     * Valida la estructura del XML contra el XSD de Pagos 2.0.
     *
     * @param xmlContent Contenido XML
     * @return true si es válido, false en caso contrario
     */
    boolean validateXmlStructure(String xmlContent);

    /**
     * Convierte un String XML a Document.
     *
     * @param xmlContent Contenido XML
     * @return Document parseado
     * @throws RuntimeException si hay error en la conversión
     */
    Document xmlStringToDocument(String xmlContent);
}

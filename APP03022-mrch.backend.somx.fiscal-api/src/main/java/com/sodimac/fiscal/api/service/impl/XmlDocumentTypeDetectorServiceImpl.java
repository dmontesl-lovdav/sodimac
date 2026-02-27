package com.sodimac.fiscal.api.service.impl;

import com.sodimac.fiscal.api.model.enums.TipoDocumentoFiscal;
import com.sodimac.fiscal.api.service.XmlDocumentTypeDetector;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import java.io.ByteArrayInputStream;
import java.nio.charset.StandardCharsets;

/**
 * Implementación del servicio para detectar el tipo de documento fiscal XML.
 *
 * Analiza el contenido XML para identificar automáticamente el tipo
 * de comprobante fiscal según las reglas del SAT mexicano.
 *
 * @author g_dco018
 * @version 1.0
 * @since 2025
 */
@Service
@Slf4j
public class XmlDocumentTypeDetectorServiceImpl implements XmlDocumentTypeDetector {

    /**
     * {@inheritDoc}
     */
    @Override
    public TipoDocumentoFiscal detectDocumentType(String xmlContent) {
        try {
            Document document = parseXml(xmlContent);
            Element rootElement = document.getDocumentElement();

            // Detectar por elemento raíz
            String rootElementName = rootElement.getLocalName();
            log.debug("Elemento raíz detectado: {}", rootElementName);

            switch (rootElementName) {
                case "Comprobante":
                    return detectComprobanteType(rootElement);
                case "Pagos":
                    return TipoDocumentoFiscal.COMPLEMENTO_PAGO;
                default:
                    throw new RuntimeException("Tipo de documento XML no soportado: " + rootElementName);
            }

        } catch (Exception e) {
            log.error("Error detectando tipo de documento XML", e);
            throw new RuntimeException("Error procesando XML: " + e.getMessage(), e);
        }
    }

    /**
     * Detecta el tipo específico de comprobante CFDI.
     *
     * @param comprobanteElement Elemento Comprobante del XML
     * @return Tipo de documento fiscal
     */
    private TipoDocumentoFiscal detectComprobanteType(Element comprobanteElement) {
        String tipoComprobante = comprobanteElement.getAttribute("TipoDeComprobante");
        log.debug("TipoDeComprobante detectado: {}", tipoComprobante);

        // Verificar si tiene complemento CartaPorte
        if (hasCartaPorteComplement(comprobanteElement)) {
            log.debug("Complemento CartaPorte detectado");
            return TipoDocumentoFiscal.FACTURA_CARTA_PORTE;
        }

        // Clasificar por tipo de comprobante
        switch (tipoComprobante) {
            case "I":
                return TipoDocumentoFiscal.FACTURA;
            case "E":
                return TipoDocumentoFiscal.NOTA_CREDITO;
            case "T":
                return TipoDocumentoFiscal.FACTURA_CARTA_PORTE;
            case "P":
                return TipoDocumentoFiscal.COMPLEMENTO_PAGO;
            default:
                throw new RuntimeException("Tipo de comprobante no soportado: " + tipoComprobante);
        }
    }

    /**
     * Verifica si el comprobante contiene el complemento CartaPorte.
     *
     * @param comprobanteElement Elemento Comprobante
     * @return true si contiene CartaPorte, false en caso contrario
     */
    private boolean hasCartaPorteComplement(Element comprobanteElement) {
        // Buscar elemento Complemento
        NodeList complementos = comprobanteElement.getElementsByTagName("cfdi:Complemento");
        if (complementos.getLength() == 0) {
            complementos = comprobanteElement.getElementsByTagName("Complemento");
        }

        for (int i = 0; i < complementos.getLength(); i++) {
            Element complemento = (Element) complementos.item(i);

            // Buscar CartaPorte dentro del complemento
            NodeList cartaPortes = complemento.getElementsByTagName("cartaporte31:CartaPorte");
            if (cartaPortes.getLength() == 0) {
                cartaPortes = complemento.getElementsByTagName("CartaPorte");
            }

            if (cartaPortes.getLength() > 0) {
                return true;
            }
        }

        return false;
    }

    /**
     * Parsea el contenido XML a un Document.
     *
     * @param xmlContent Contenido XML
     * @return Document parseado
     * @throws Exception si hay error en el parseo
     */
    private Document parseXml(String xmlContent) throws Exception {
        DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
        factory.setNamespaceAware(true);
        factory.setIgnoringElementContentWhitespace(true);

        DocumentBuilder builder = factory.newDocumentBuilder();
        return builder.parse(new ByteArrayInputStream(xmlContent.getBytes(StandardCharsets.UTF_8)));
    }
}

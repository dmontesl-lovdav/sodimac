package com.sodimac.fiscal.api.service.impl;

import com.sodimac.fiscal.api.model.dto.response.*;
import com.sodimac.fiscal.api.model.enums.TipoDocumentoFiscal;
import com.sodimac.fiscal.api.service.FiscalXmlTransformerService;
import com.sodimac.fiscal.api.service.XmlDocumentTypeDetector;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

import com.sodimac.fiscal.api.util.XmlSecureFactory;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import java.io.ByteArrayInputStream;
import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;

/**
 * Implementación del servicio para transformar documentos XML fiscales a respuesta JSON estructurada.
 *
 * Extrae específicamente los nodos: Comprobante, Emisor, Receptor y
 * Complemento->TimbreFiscalDigital con todos sus campos relacionados.
 *
 * @author g_dco018
 * @version 1.0
 * @since 2025
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class FiscalXmlTransformerServiceImpl implements FiscalXmlTransformerService {

    private final XmlDocumentTypeDetector xmlDocumentTypeDetector;

    // TipoRelacion SAT "01" = Nota de crédito de los documentos relacionados. Único bloque válido para la relación NC->Factura.
    private static final String TIPO_RELACION_NC = "01";

    /**
     * {@inheritDoc}
     */
    @Override
    public FiscalXmlResponse transformToStructuredResponse(String xmlContent) {
        log.info("Iniciando transformación de XML fiscal a respuesta estructurada");

        try {
            // 1. Detectar tipo de documento usando detector
            TipoDocumentoFiscal tipoDocumento = xmlDocumentTypeDetector.detectDocumentType(xmlContent);

            // 2. Parsear XML
            Document document = parseXml(xmlContent);

            // 3. Extraer nodos específicos
            FiscalXmlResponse response = FiscalXmlResponse.builder()
                    .comprobante(extractComprobanteInfo(document))
                    .emisor(extractEmisorInfo(document))
                    .receptor(extractReceptorInfo(document))
                    .timbreFiscalDigital(extractTimbreFiscalDigital(document))
                    .metadatos(buildMetadatos(tipoDocumento, document))
                    .build();

            log.info("Transformación completada exitosamente para tipo: {}", tipoDocumento);
            return response;

        } catch (Exception e) {
            log.error("Error transformando XML fiscal", e);
            throw new RuntimeException("Error en la transformación del XML: " + e.getMessage(), e);
        }
    }

    /**
     * Extrae información del nodo Comprobante.
     */
    private ComprobanteResponse extractComprobanteInfo(Document document) {
        Element comprobante = getFirstElementByTagName(document, "Comprobante", "cfdi:Comprobante");

        if (comprobante == null) {
            log.warn("No se encontró el nodo Comprobante");
            return null;
        }

        // CfdiRelacionados (NC): solo se toma el bloque con TipoRelacion="01" (NC de documentos relacionados).
        // Puede haber varios bloques (04, 03, etc.); los que no son "01" se ignoran. Regla Ivan 2026-07-17.
        String tipoRelacion = null;
        String uuidRelacionado = null;
        Element cfdiRelacionados = getCfdiRelacionadosByTipo(document, TIPO_RELACION_NC);
        if (cfdiRelacionados != null) {
            tipoRelacion = getAttribute(cfdiRelacionados, "TipoRelacion");
            Element cfdiRelacionado = getFirstElementByTagName(cfdiRelacionados, "cfdi:CfdiRelacionado", "CfdiRelacionado");
            if (cfdiRelacionado != null) {
                uuidRelacionado = getAttribute(cfdiRelacionado, "UUID");
            }
        }

        return ComprobanteResponse.builder()
                .tipoRelacion(tipoRelacion)
                .uuidRelacionado(uuidRelacionado)
                .version(getAttribute(comprobante, "Version"))
                .serie(getAttribute(comprobante, "Serie"))
                .folio(getAttribute(comprobante, "Folio"))
                .fecha(getAttribute(comprobante, "Fecha"))
                .subTotal(getAttribute(comprobante, "SubTotal"))
                .total(getAttribute(comprobante, "Total"))
                .tipoDeComprobante(getAttribute(comprobante, "TipoDeComprobante"))
                .metodoPago(getAttribute(comprobante, "MetodoPago"))
                .formaPago(getAttribute(comprobante, "FormaPago"))
                .condicionesDePago(getAttribute(comprobante, "CondicionesDePago"))
                .moneda(getAttribute(comprobante, "Moneda"))
                .tipoCambio(getAttribute(comprobante, "TipoCambio"))
                .noCertificado(getAttribute(comprobante, "NoCertificado"))
                .certificado(getAttribute(comprobante, "Certificado"))
                .sello(getAttribute(comprobante, "Sello"))
                .lugarExpedicion(getAttribute(comprobante, "LugarExpedicion"))
                .exportacion(getAttribute(comprobante, "Exportacion"))
                .descuento(getAttribute(comprobante, "Descuento"))
                .build();
    }

    /**
     * Extrae información del nodo Emisor.
     */
    private EmisorResponse extractEmisorInfo(Document document) {
        Element emisor = getFirstElementByTagName(document, "Emisor", "cfdi:Emisor");

        if (emisor == null) {
            log.warn("No se encontró el nodo Emisor");
            return null;
        }

        return EmisorResponse.builder()
                .rfc(getAttribute(emisor, "Rfc"))
                .nombre(getAttribute(emisor, "Nombre"))
                .regimenFiscal(getAttribute(emisor, "RegimenFiscal"))
                .facAtrAdquirente(getAttribute(emisor, "FacAtrAdquirente"))
                .build();
    }

    /**
     * Extrae información del nodo Receptor.
     */
    private ReceptorResponse extractReceptorInfo(Document document) {
        Element receptor = getFirstElementByTagName(document, "Receptor", "cfdi:Receptor");

        if (receptor == null) {
            log.warn("No se encontró el nodo Receptor");
            return null;
        }

        return ReceptorResponse.builder()
                .rfc(getAttribute(receptor, "Rfc"))
                .nombre(getAttribute(receptor, "Nombre"))
                .domicilioFiscalReceptor(getAttribute(receptor, "DomicilioFiscalReceptor"))
                .regimenFiscalReceptor(getAttribute(receptor, "RegimenFiscalReceptor"))
                .usoCFDI(getAttribute(receptor, "UsoCFDI"))
                .residenciaFiscal(getAttribute(receptor, "ResidenciaFiscal"))
                .numRegIdTrib(getAttribute(receptor, "NumRegIdTrib"))
                .build();
    }

    /**
     * Extrae información del complemento TimbreFiscalDigital.
     */
    private TimbreFiscalDigitalResponse extractTimbreFiscalDigital(Document document) {
        // Buscar en complementos
        NodeList complementos = document.getElementsByTagName("cfdi:Complemento");
        if (complementos.getLength() == 0) {
            complementos = document.getElementsByTagName("Complemento");
        }

        for (int i = 0; i < complementos.getLength(); i++) {
            Element complemento = (Element) complementos.item(i);

            // Buscar TimbreFiscalDigital
            Element timbre = getFirstElementByTagName(complemento,
                    "TimbreFiscalDigital",
                    "tfd:TimbreFiscalDigital");

            if (timbre != null) {
                return TimbreFiscalDigitalResponse.builder()
                        .version(getAttribute(timbre, "Version"))
                        .uuid(getAttribute(timbre, "UUID"))
                        .fechaTimbrado(getAttribute(timbre, "FechaTimbrado"))
                        .rfcProvCertif(getAttribute(timbre, "RfcProvCertif"))
                        .leyenda(getAttribute(timbre, "Leyenda"))
                        .selloCFD(getAttribute(timbre, "SelloCFD"))
                        .noCertificadoSAT(getAttribute(timbre, "NoCertificadoSAT"))
                        .selloSAT(getAttribute(timbre, "SelloSAT"))
                        .build();
            }
        }

        log.warn("No se encontró el complemento TimbreFiscalDigital");
        return null;
    }

    /**
     * Construye los metadatos del procesamiento.
     */
    private MetadatosResponse buildMetadatos(TipoDocumentoFiscal tipoDocumento, Document document) {
        boolean tieneComplementos = hasComplements(document);

        return MetadatosResponse.builder()
                .tipoDocumento(tipoDocumento.name())
                .descripcionTipo(tipoDocumento.toString())
                .version("Automática")
                .xsdPath(null)
                .namespace(null)
                .tieneComplementos(tieneComplementos)
                .complementoPrincipal(tieneComplementos ? "Detectado" : null)
                .fechaProcesamiento(LocalDateTime.now())
                .estado("SUCCESS")
                .mensaje("XML procesado correctamente")
                .build();
    }

    /**
     * Verifica si el documento tiene complementos.
     */
    private boolean hasComplements(Document document) {
        NodeList complementos = document.getElementsByTagName("cfdi:Complemento");
        if (complementos.getLength() == 0) {
            complementos = document.getElementsByTagName("Complemento");
        }
        return complementos.getLength() > 0;
    }

    /**
     * Obtiene el primer elemento por nombre de tag (con fallbacks).
     */
    private Element getFirstElementByTagName(Document document, String... tagNames) {
        for (String tagName : tagNames) {
            NodeList elements = document.getElementsByTagName(tagName);
            if (elements.getLength() > 0) {
                return (Element) elements.item(0);
            }
        }
        return null;
    }

    /**
     * Devuelve el nodo CfdiRelacionados cuyo TipoRelacion coincide (ej. "01" para NC de documentos
     * relacionados). Recorre todos los bloques y retorna el primero que haga match; null si no hay.
     */
    private Element getCfdiRelacionadosByTipo(Document document, String tipoRelacion) {
        NodeList nodes = document.getElementsByTagName("cfdi:CfdiRelacionados");
        if (nodes.getLength() == 0) {
            nodes = document.getElementsByTagName("CfdiRelacionados");
        }
        for (int i = 0; i < nodes.getLength(); i++) {
            Element el = (Element) nodes.item(i);
            if (tipoRelacion.equals(getAttribute(el, "TipoRelacion"))) {
                return el;
            }
        }
        return null;
    }

    /**
     * Obtiene el primer elemento por nombre de tag dentro de un elemento padre.
     */
    private Element getFirstElementByTagName(Element parent, String... tagNames) {
        for (String tagName : tagNames) {
            NodeList elements = parent.getElementsByTagName(tagName);
            if (elements.getLength() > 0) {
                return (Element) elements.item(0);
            }
        }
        return null;
    }

    /**
     * Obtiene un atributo de un elemento, retorna null si no existe.
     */
    private String getAttribute(Element element, String attributeName) {
        if (element == null) {
            return null;
        }
        String value = element.getAttribute(attributeName);
        return value.isEmpty() ? null : value;
    }

    /**
     * Parsea el contenido XML a un Document.
     */
    private Document parseXml(String xmlContent) throws Exception {
        DocumentBuilderFactory factory = XmlSecureFactory.newDocumentBuilderFactory();
        factory.setNamespaceAware(true);
        factory.setIgnoringElementContentWhitespace(true);

        DocumentBuilder builder = factory.newDocumentBuilder();
        return builder.parse(new ByteArrayInputStream(xmlContent.getBytes(StandardCharsets.UTF_8)));
    }
}

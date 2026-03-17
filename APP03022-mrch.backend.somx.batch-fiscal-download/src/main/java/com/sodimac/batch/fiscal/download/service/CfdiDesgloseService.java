package com.sodimac.batch.fiscal.download.service;

import com.sodimac.batch.fiscal.download.mapper.*;
import com.sodimac.batch.fiscal.download.model.entity.sap.*;
import com.sodimac.batch.fiscal.download.repository.sap.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.w3c.dom.*;
import org.xml.sax.InputSource;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import java.io.StringReader;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

@Service
public class CfdiDesgloseService {

    private static final Logger log = LoggerFactory.getLogger(CfdiDesgloseService.class);

    private final ComprobanteRepository comprobanteRepository;
    private final EmisorRepository emisorRepository;
    private final ReceptorRepository receptorRepository;
    private final ConceptoRepository conceptoRepository;
    private final ImpuestosRepository impuestosRepository;
    private final TrasladoRepository trasladoRepository;
    private final RetencionRepository retencionRepository;
    private final DetalleImpuestoRepository detalleImpuestoRepository;

    public CfdiDesgloseService(ComprobanteRepository comprobanteRepository,
                                EmisorRepository emisorRepository,
                                ReceptorRepository receptorRepository,
                                ConceptoRepository conceptoRepository,
                                ImpuestosRepository impuestosRepository,
                                TrasladoRepository trasladoRepository,
                                RetencionRepository retencionRepository,
                                DetalleImpuestoRepository detalleImpuestoRepository) {
        this.comprobanteRepository = comprobanteRepository;
        this.emisorRepository = emisorRepository;
        this.receptorRepository = receptorRepository;
        this.conceptoRepository = conceptoRepository;
        this.impuestosRepository = impuestosRepository;
        this.trasladoRepository = trasladoRepository;
        this.retencionRepository = retencionRepository;
        this.detalleImpuestoRepository = detalleImpuestoRepository;
    }

    private static final String NS_CFDI = "http://www.sat.gob.mx/cfd/4";
    private static final String NS_TFD = "http://www.sat.gob.mx/TimbreFiscalDigital";
    private static final DateTimeFormatter CFDI_DATE = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss");

    @Transactional("sapTransactionManager")
    public ComprobanteEntity desglosar(String xmlContent, String invoiceUuid) throws Exception {
        Document doc = parseXml(xmlContent);
        Element root = doc.getDocumentElement();

        // 1. Comprobante
        ComprobanteEntity comprobante = extractComprobante(root, xmlContent, invoiceUuid);

        // Extraer TimbreFiscalDigital
        NodeList tfdNodes = doc.getElementsByTagNameNS(NS_TFD, "TimbreFiscalDigital");
        if (tfdNodes.getLength() > 0) {
            Element tfd = (Element) tfdNodes.item(0);
            comprobante.setFiscalUuid(attr(tfd, "UUID"));
            comprobante.setFechaTimbrado(parseDateTime(attr(tfd, "FechaTimbrado")));
            comprobante.setRfcProvCertif(attr(tfd, "RfcProvCertif"));
            comprobante.setNoCertificadoSat(attr(tfd, "NoCertificadoSAT"));
        }

        // Verificar si ya existe
        if (comprobanteRepository.existsByFiscalUuid(comprobante.getFiscalUuid())) {
            log.warn("Comprobante ya existe en SAP_DEV: {}", comprobante.getFiscalUuid());
            return comprobanteRepository.findByFiscalUuid(comprobante.getFiscalUuid()).orElse(null);
        }

        comprobante = comprobanteRepository.save(comprobante);
        int idComp = comprobante.getIdComprobante();

        // 2. Emisor
        NodeList emisorNodes = doc.getElementsByTagNameNS(NS_CFDI, "Emisor");
        if (emisorNodes.getLength() > 0) {
            extractEmisor((Element) emisorNodes.item(0), idComp);
        }

        // 3. Receptor
        NodeList receptorNodes = doc.getElementsByTagNameNS(NS_CFDI, "Receptor");
        if (receptorNodes.getLength() > 0) {
            extractReceptor((Element) receptorNodes.item(0), idComp);
        }

        // 4. Conceptos
        NodeList conceptoNodes = doc.getElementsByTagNameNS(NS_CFDI, "Concepto");
        for (int i = 0; i < conceptoNodes.getLength(); i++) {
            extractConcepto((Element) conceptoNodes.item(i), idComp);
        }

        // 5. Impuestos (nivel comprobante)
        NodeList impuestosNodes = root.getElementsByTagNameNS(NS_CFDI, "Impuestos");
        for (int i = 0; i < impuestosNodes.getLength(); i++) {
            Element impNode = (Element) impuestosNodes.item(i);
            if (impNode.getParentNode().getLocalName().equals("Comprobante")) {
                extractImpuestos(impNode, idComp);
            }
        }

        log.info("Desglose completado: uuid={} comprobante_id={}", comprobante.getFiscalUuid(), idComp);
        return comprobante;
    }

    private ComprobanteEntity extractComprobante(Element root, String xmlContent, String invoiceUuid) {
        return ComprobanteMapper.toEntity(
                invoiceUuid,
                attr(root, "Version"),
                attr(root, "Serie"),
                attr(root, "Folio"),
                parseDateTime(attr(root, "Fecha")),
                decimal(attr(root, "SubTotal")),
                decimal(attr(root, "Total")),
                decimal(attr(root, "Descuento")),
                attrOr(root, "Moneda", "MXN"),
                decimal(attr(root, "TipoCambio")),
                attr(root, "TipoDeComprobante"),
                attr(root, "MetodoPago"),
                attr(root, "FormaPago"),
                attr(root, "CondicionesDePago"),
                attr(root, "LugarExpedicion"),
                attr(root, "Exportacion"),
                attr(root, "NoCertificado"),
                attr(root, "Sello"),
                attr(root, "Certificado"),
                xmlContent);
    }

    private void extractEmisor(Element node, int idComprobante) {
        EmisorEntity emisor = EmisorMapper.toEntity(
                idComprobante, attr(node, "Rfc"),
                attr(node, "Nombre"), attr(node, "RegimenFiscal"));
        emisorRepository.save(emisor);
    }

    private void extractReceptor(Element node, int idComprobante) {
        ReceptorEntity receptor = ReceptorMapper.toEntity(
                idComprobante, attr(node, "Rfc"),
                attr(node, "Nombre"), attr(node, "UsoCFDI"),
                attr(node, "RegimenFiscalReceptor"),
                attr(node, "DomicilioFiscalReceptor"));
        receptorRepository.save(receptor);
    }

    private void extractConcepto(Element node, int idComprobante) {
        ConceptoEntity concepto = ConceptoMapper.toEntity(
                idComprobante,
                attr(node, "ClaveProdServ"),
                attr(node, "NoIdentificacion"),
                decimal(attr(node, "Cantidad")),
                attr(node, "ClaveUnidad"),
                attr(node, "Unidad"),
                attr(node, "Descripcion"),
                decimal(attr(node, "ValorUnitario")),
                decimal(attr(node, "Importe")),
                decimal(attr(node, "Descuento")),
                attr(node, "ObjetoImp"));
        concepto = conceptoRepository.save(concepto);

        // Impuestos a nivel concepto (DetalleImpuesto)
        NodeList impuestosNodes = node.getElementsByTagNameNS(NS_CFDI, "Impuestos");
        if (impuestosNodes.getLength() > 0) {
            Element impuestos = (Element) impuestosNodes.item(0);

            NodeList traslados = impuestos.getElementsByTagNameNS(NS_CFDI, "Traslado");
            for (int i = 0; i < traslados.getLength(); i++) {
                saveDetalleImpuesto((Element) traslados.item(i), concepto.getIdConcepto(), "TRASLADO");
            }

            NodeList retenciones = impuestos.getElementsByTagNameNS(NS_CFDI, "Retencion");
            for (int i = 0; i < retenciones.getLength(); i++) {
                saveDetalleImpuesto((Element) retenciones.item(i), concepto.getIdConcepto(), "RETENCION");
            }
        }
    }

    private void saveDetalleImpuesto(Element node, int idConcepto, String tipo) {
        DetalleImpuestoEntity detalle = DetalleImpuestoMapper.toEntity(
                idConcepto, tipo,
                decimal(attr(node, "Base")),
                attr(node, "Impuesto"),
                attr(node, "TipoFactor"),
                decimal(attr(node, "TasaOCuota")),
                decimal(attr(node, "Importe")));
        detalleImpuestoRepository.save(detalle);
    }

    private void extractImpuestos(Element node, int idComprobante) {
        ImpuestosEntity impuestos = ImpuestosMapper.toEntity(
                idComprobante,
                decimal(attr(node, "TotalImpuestosTrasladados")),
                decimal(attr(node, "TotalImpuestosRetenidos")));
        impuestos = impuestosRepository.save(impuestos);

        // Traslados a nivel comprobante
        NodeList trasladosWrapper = node.getElementsByTagNameNS(NS_CFDI, "Traslados");
        if (trasladosWrapper.getLength() > 0) {
            NodeList traslados = ((Element) trasladosWrapper.item(0)).getElementsByTagNameNS(NS_CFDI, "Traslado");
            for (int i = 0; i < traslados.getLength(); i++) {
                saveTraslado((Element) traslados.item(i), impuestos.getIdImpuesto());
            }
        }

        // Retenciones a nivel comprobante
        NodeList retencionesWrapper = node.getElementsByTagNameNS(NS_CFDI, "Retenciones");
        if (retencionesWrapper.getLength() > 0) {
            NodeList retenciones = ((Element) retencionesWrapper.item(0)).getElementsByTagNameNS(NS_CFDI, "Retencion");
            for (int i = 0; i < retenciones.getLength(); i++) {
                saveRetencion((Element) retenciones.item(i), impuestos.getIdImpuesto());
            }
        }
    }

    private void saveTraslado(Element node, int idImpuesto) {
        TrasladoEntity traslado = TrasladoMapper.toEntity(
                idImpuesto, decimal(attr(node, "Base")),
                attr(node, "Impuesto"), attr(node, "TipoFactor"),
                decimal(attr(node, "TasaOCuota")),
                decimal(attr(node, "Importe")));
        trasladoRepository.save(traslado);
    }

    private void saveRetencion(Element node, int idImpuesto) {
        RetencionEntity retencion = RetencionMapper.toEntity(
                idImpuesto, decimal(attr(node, "Base")),
                attr(node, "Impuesto"), attr(node, "TipoFactor"),
                decimal(attr(node, "TasaOCuota")),
                decimal(attr(node, "Importe")));
        retencionRepository.save(retencion);
    }

    // ── Validacion de Addenda ───────────────────────────────────

    public List<String> validarAddenda(String xmlContent, String tipoDocumento) {
        List<String> errores = new ArrayList<>();
        try {
            Document doc = parseXml(xmlContent);
            NodeList addendas = doc.getElementsByTagNameNS(NS_CFDI, "Addenda");
            if (addendas.getLength() == 0) {
                addendas = doc.getElementsByTagName("cfdi:Addenda");
            }
            if (addendas.getLength() == 0) {
                errores.add("El documento no contiene nodo Addenda");
                return errores;
            }

            if ("I".equals(tipoDocumento)) {
                if (!containsAddendaField(addendas.item(0), "IdProveedor"))
                    errores.add("Falta IdProveedor en Addenda");
                if (!containsAddendaField(addendas.item(0), "TipoProveedor"))
                    errores.add("Falta TipoProveedor en Addenda");
                if (!containsAddendaField(addendas.item(0), "OrdenCompra"))
                    errores.add("Falta OrdenCompra en Addenda");
                if (!containsAddendaField(addendas.item(0), "Recepcion"))
                    errores.add("Falta Recepcion en Addenda");
            } else if ("E".equals(tipoDocumento)) {
                if (!containsAddendaField(addendas.item(0), "IdProveedor"))
                    errores.add("Falta IdProveedor en Addenda");
                if (!containsAddendaField(addendas.item(0), "TipoProveedor"))
                    errores.add("Falta TipoProveedor en Addenda");
                if (!containsAddendaField(addendas.item(0), "TipoNC"))
                    errores.add("Falta TipoNC en Addenda");
            }
        } catch (Exception e) {
            errores.add("Error al parsear addenda: " + e.getMessage());
        }
        return errores;
    }

    private boolean containsAddendaField(Node addendaNode, String fieldName) {
        if (addendaNode instanceof Element) {
            Element elem = (Element) addendaNode;
            NodeList children = elem.getElementsByTagName("*");
            for (int i = 0; i < children.getLength(); i++) {
                Element child = (Element) children.item(i);
                if (child.getLocalName() != null && child.getLocalName().equalsIgnoreCase(fieldName)) {
                    return true;
                }
                if (child.getTagName().endsWith(":" + fieldName)) {
                    return true;
                }
                if (child.hasAttribute(fieldName) && !child.getAttribute(fieldName).isEmpty()) {
                    return true;
                }
            }
        }
        return false;
    }

    // ── Utilidades ──────────────────────────────────────────────

    private Document parseXml(String xmlContent) throws Exception {
        DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
        factory.setNamespaceAware(true);
        DocumentBuilder builder = factory.newDocumentBuilder();
        return builder.parse(new InputSource(new StringReader(xmlContent)));
    }

    private String attr(Element el, String name) {
        String val = el.getAttribute(name);
        return (val == null || val.isEmpty()) ? null : val;
    }

    private String attrOr(Element el, String name, String defaultValue) {
        String val = attr(el, name);
        return val != null ? val : defaultValue;
    }

    private BigDecimal decimal(String value) {
        if (value == null || value.isEmpty()) return null;
        try {
            return new BigDecimal(value);
        } catch (NumberFormatException e) {
            return null;
        }
    }

    private LocalDateTime parseDateTime(String value) {
        if (value == null || value.isEmpty()) return null;
        try {
            return LocalDateTime.parse(value, CFDI_DATE);
        } catch (Exception e) {
            return null;
        }
    }
}

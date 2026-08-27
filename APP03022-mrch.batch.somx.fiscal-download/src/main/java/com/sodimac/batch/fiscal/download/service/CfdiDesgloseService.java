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

/**
 * Desglose de CFDI hacia el esquema SAP legado (SODIMAC_SAP_DEV, Detecno).
 *
 * Modelo legado: todas las tablas se ligan por Uuid (folio fiscal SAT); no hay FKs de
 * identidad entre Comprobante/Emisor/Addenda. Concepto genera IdPadre (identity) y
 * DetalleImpuesto referencia ese IdPadre junto con Uuid y ClaveProdServ. No existe tabla
 * Receptor (el receptor siempre es Sodimac) ni tablas de impuestos a nivel comprobante:
 * los totales se derivan de DetalleImpuesto o del Xml completo.
 */
@Service
public class CfdiDesgloseService {

    private static final Logger log = LoggerFactory.getLogger(CfdiDesgloseService.class);

    private final ComprobanteRepository comprobanteRepository;
    private final EmisorRepository emisorRepository;
    private final ConceptoRepository conceptoRepository;
    private final DetalleImpuestoRepository detalleImpuestoRepository;
    private final AddendaRepository addendaRepository;

    public CfdiDesgloseService(ComprobanteRepository comprobanteRepository,
                                EmisorRepository emisorRepository,
                                ConceptoRepository conceptoRepository,
                                DetalleImpuestoRepository detalleImpuestoRepository,
                                AddendaRepository addendaRepository) {
        this.comprobanteRepository = comprobanteRepository;
        this.emisorRepository = emisorRepository;
        this.conceptoRepository = conceptoRepository;
        this.detalleImpuestoRepository = detalleImpuestoRepository;
        this.addendaRepository = addendaRepository;
    }

    private static final String NS_CFDI = "http://www.sat.gob.mx/cfd/4";
    private static final String NS_TFD = "http://www.sat.gob.mx/TimbreFiscalDigital";
    private static final DateTimeFormatter CFDI_DATE = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss");

    @Transactional("sapTransactionManager")
    public ComprobanteEntity desglosar(String xmlContent, String invoiceUuid) throws Exception {
        Document doc;
        try {
            doc = parseXml(xmlContent);
        } catch (Exception e) {
            throw new CfdiEstructuraException("XML no parseable: " + e.getMessage(), e);
        }
        Element root = doc.getDocumentElement();

        // UUID del TimbreFiscalDigital: es la PK del esquema legado; sin él no hay desglose posible.
        String uuid = extractTfdUuid(doc);
        if (uuid == null || uuid.isEmpty()) {
            throw new CfdiEstructuraException("CFDI sin UUID de TimbreFiscalDigital");
        }

        // Verificar si ya existe (idempotencia por Uuid)
        if (comprobanteRepository.existsById(uuid)) {
            log.warn("Comprobante ya existe en SAP_DEV: {}", uuid);
            return comprobanteRepository.findById(uuid).orElse(null);
        }

        // 1. Comprobante
        ComprobanteEntity comprobante = extractComprobante(root, uuid, xmlContent, invoiceUuid);
        comprobante = comprobanteRepository.save(comprobante);

        // 2. Emisor
        NodeList emisorNodes = doc.getElementsByTagNameNS(NS_CFDI, "Emisor");
        if (emisorNodes.getLength() > 0) {
            extractEmisor((Element) emisorNodes.item(0), uuid);
        }

        // 3. Conceptos (el receptor no se persiste: no existe tabla Receptor en el esquema legado)
        NodeList conceptoNodes = doc.getElementsByTagNameNS(NS_CFDI, "Concepto");
        for (int i = 0; i < conceptoNodes.getLength(); i++) {
            extractConcepto((Element) conceptoNodes.item(i), uuid);
        }

        // 4. Addenda (dentro de cfdi:Addenda). Se liga por fiscal_uuid (Uuid).
        // 3 variantes según el nodo: Tipo 1 mercancía, Tipo 2 transporte, Tipo 3 NC.
        extractAddenda(doc, uuid);

        log.info("Desglose completado: uuid={}", uuid);
        return comprobante;
    }

    private String extractTfdUuid(Document doc) {
        NodeList tfdNodes = doc.getElementsByTagNameNS(NS_TFD, "TimbreFiscalDigital");
        if (tfdNodes.getLength() == 0) {
            return null;
        }
        return attr((Element) tfdNodes.item(0), "UUID");
    }

    private ComprobanteEntity extractComprobante(Element root, String uuid,
                                                  String xmlContent, String invoiceUuid) {
        return ComprobanteMapper.toEntity(
                uuid,
                invoiceUuid,
                attr(root, "Version"),
                attr(root, "Serie"),
                attr(root, "Folio"),
                parseDateTime(attr(root, "Fecha")),
                decimal(attr(root, "SubTotal")),
                decimal(attr(root, "Total")),
                decimal(attr(root, "Descuento")),
                attrOr(root, "Moneda", "MXN"),
                attr(root, "TipoCambio"),
                attr(root, "TipoDeComprobante"),
                attr(root, "MetodoPago"),
                attr(root, "FormaPago"),
                attr(root, "CondicionesDePago"),
                attr(root, "LugarExpedicion"),
                xmlContent);
    }

    private void extractEmisor(Element node, String uuid) {
        EmisorEntity emisor = EmisorMapper.toEntity(
                uuid, attr(node, "Rfc"),
                attr(node, "Nombre"), attr(node, "RegimenFiscal"));
        emisorRepository.save(emisor);
    }

    private void extractConcepto(Element node, String uuid) {
        ConceptoEntity concepto = ConceptoMapper.toEntity(
                uuid,
                attr(node, "ClaveProdServ"),
                attr(node, "Cantidad"),
                attr(node, "ClaveUnidad"),
                // Unidad es NOT NULL en el esquema legado pero opcional en CFDI 4.0.
                attrOr(node, "Unidad", attr(node, "ClaveUnidad")),
                attr(node, "Descripcion"),
                decimal(attr(node, "ValorUnitario")),
                decimal(attr(node, "Importe")));
        concepto = conceptoRepository.save(concepto);

        // Impuestos a nivel concepto (DetalleImpuesto), ligados por Uuid + IdPadre + ClaveProdServ
        NodeList impuestosNodes = node.getElementsByTagNameNS(NS_CFDI, "Impuestos");
        if (impuestosNodes.getLength() > 0) {
            Element impuestos = (Element) impuestosNodes.item(0);

            NodeList traslados = impuestos.getElementsByTagNameNS(NS_CFDI, "Traslado");
            for (int i = 0; i < traslados.getLength(); i++) {
                saveDetalleImpuesto((Element) traslados.item(i), concepto,
                        DetalleImpuestoEntity.TIPO_TRASLADO);
            }

            NodeList retenciones = impuestos.getElementsByTagNameNS(NS_CFDI, "Retencion");
            for (int i = 0; i < retenciones.getLength(); i++) {
                saveDetalleImpuesto((Element) retenciones.item(i), concepto,
                        DetalleImpuestoEntity.TIPO_RETENCION);
            }
        }
    }

    private void saveDetalleImpuesto(Element node, ConceptoEntity concepto, String tipoImpuesto) {
        BigDecimal base = decimal(attr(node, "Base"));
        BigDecimal tasaOCuota = decimal(attr(node, "TasaOCuota"));
        BigDecimal importe = decimal(attr(node, "Importe"));
        // Columnas NOT NULL en el esquema legado; los traslados Exentos no traen tasa ni importe.
        if (base == null || tasaOCuota == null || importe == null) {
            log.warn("Impuesto sin Base/TasaOCuota/Importe (¿Exento?) omitido: uuid={} claveProdServ={}",
                    concepto.getUuid(), concepto.getClaveProdServ());
            return;
        }
        DetalleImpuestoEntity detalle = DetalleImpuestoMapper.toEntity(
                concepto.getUuid(), concepto.getIdPadre(), concepto.getClaveProdServ(),
                tipoImpuesto, base,
                attr(node, "Impuesto"),
                attr(node, "TipoFactor"),
                tasaOCuota, importe);
        detalleImpuestoRepository.save(detalle);
    }

    // ── Persistencia de Addenda (Addenda_Sodimac_Detecno, Tipo=1) ──

    /**
     * Extrae la addenda del CFDI (dentro de {@code cfdi:Addenda}) y la guarda en la tabla destino
     * Addenda, ligada por fiscal_uuid. Idempotente: si ya existe, no reescribe. Despacha según el
     * nodo presente:
     * - {@code Addenda_Sodimac_Detecno} → Tipo 1 (mercancía).
     * - {@code Addenda_Transportistas_Sodimac_Detecno} → Tipo 2 (transporte/CartaPorte).
     * - NC (Tipo 3): pendiente confirmar nombre de nodo real; ver {@link #extractAddendaNc}.
     */
    private void extractAddenda(Document doc, String fiscalUuid) {
        if (fiscalUuid == null || fiscalUuid.isEmpty()) {
            return;
        }
        if (addendaRepository.existsById(fiscalUuid)) {
            log.warn("Addenda ya existe en destino: {}", fiscalUuid);
            return;
        }

        // Tipo 1: mercancía
        NodeList detecno = doc.getElementsByTagName("Addenda_Sodimac_Detecno");
        if (detecno.getLength() > 0) {
            Element a = (Element) detecno.item(0);
            // Extra4 = Serie + Folio del comprobante (spec Ivan). Si la Serie viene vacía queda solo el folio.
            Element root = doc.getDocumentElement();
            String serie = attr(root, "Serie");
            String folio = attr(root, "Folio");
            String serieFolio = (serie == null ? "" : serie) + (folio == null ? "" : folio);
            if (serieFolio.isEmpty()) {
                serieFolio = addendaChild(a, "Folio"); // fallback al Folio del nodo addenda
            }
            AddendaEntity addenda = AddendaMapper.toEntity(
                    fiscalUuid,
                    addendaChild(a, "Proveedor"),
                    addendaChild(a, "NoOC"),
                    addendaChild(a, "NoRecepcion"),
                    serieFolio,
                    addendaChild(a, "UUID"),
                    addendaChild(a, "RFC"),
                    1);
            addendaRepository.save(addenda);
            log.info("Addenda Tipo 1 (mercancía) guardada: uuid={} proveedor={} noOC={} noRecepcion={}",
                    fiscalUuid, addenda.getExtra1(), addenda.getExtra2(), addenda.getExtra3());
            return;
        }

        // Tipo 2: transporte / CartaPorte
        NodeList transp = doc.getElementsByTagName("Addenda_Transportistas_Sodimac_Detecno");
        if (transp.getLength() > 0) {
            Element a = (Element) transp.item(0);
            AddendaEntity addenda = AddendaMapper.toEntity(
                    fiscalUuid,
                    addendaChild(a, "IdProveedor"),   // Extra1
                    null,                              // Extra2
                    null,                              // Extra3
                    addendaChild(a, "Version"),        // Extra4 (1.0)
                    addendaChild(a, "IdGuiaEntrega"),  // Extra5
                    addendaChild(a, "IdViaje"),        // Extra6
                    2);
            addendaRepository.save(addenda);
            log.info("Addenda Tipo 2 (transporte) guardada: uuid={} idProveedor={} idGuia={} idViaje={}",
                    fiscalUuid, addenda.getExtra1(), addenda.getExtra5(), addenda.getExtra6());
            return;
        }

        // Tipo 3: NC — pendiente nombre de nodo real (ver extractAddendaNc).
        if (extractAddendaNc(doc, fiscalUuid)) {
            return;
        }

        log.debug("Sin nodo de addenda reconocido para uuid={}", fiscalUuid);
    }

    /**
     * Addenda de Nota de Crédito (Tipo 3). Nodo {@code Addenda_NotaCredito_Sodimac_Detecno} con
     * campos Version, IdProveedor, TipoNC. Mapeo real (SODIMAC_SAP_DEV.dbo.Addenda):
     * Extra1=IdProveedor, Extra4=Version (1.0), Extra6=TipoNC ("Ajuste de recepción" / "Otro").
     *
     * @return true si guardó una addenda de NC; false si no aplica.
     */
    private boolean extractAddendaNc(Document doc, String fiscalUuid) {
        NodeList nc = doc.getElementsByTagName("Addenda_NotaCredito_Sodimac_Detecno");
        if (nc.getLength() == 0) {
            return false;
        }
        Element a = (Element) nc.item(0);
        AddendaEntity addenda = AddendaMapper.toEntity(
                fiscalUuid,
                addendaChild(a, "IdProveedor"),   // Extra1
                null,                              // Extra2
                null,                              // Extra3
                addendaChild(a, "Version"),        // Extra4 (1.0)
                null,                              // Extra5
                addendaChild(a, "TipoNC"),         // Extra6 (ej. "Ajuste de recepción")
                3);
        addendaRepository.save(addenda);
        log.info("Addenda Tipo 3 (NC) guardada: uuid={} idProveedor={} tipoNC={}",
                fiscalUuid, addenda.getExtra1(), addenda.getExtra6());
        return true;
    }

    /**
     * Genera y guarda la Addenda de NC (Tipo 3) a partir de los datos registrados en el
     * portal, para NC cuyo XML no trae el nodo Addenda (Iván 2026-07-31: "el XML no
     * requiere Addenda, tú la generas"; mismo criterio que ya aplica a facturas).
     * Mismo mapeo posicional que extractAddendaNc. Idempotente por uuid.
     */
    public void guardarAddendaNcDesdePortal(String fiscalUuid, String idProveedor, String tipoNc) {
        String uuid = fiscalUuid != null ? fiscalUuid.toUpperCase() : null;
        if (uuid == null || addendaRepository.existsById(uuid)) {
            log.debug("Addenda ya existente o uuid nulo ({}), no se regenera", uuid);
            return;
        }
        AddendaEntity addenda = AddendaMapper.toEntity(
                uuid,
                idProveedor,   // Extra1 = IdProveedor
                null,          // Extra2
                null,          // Extra3
                "1.0",         // Extra4 = Version
                null,          // Extra5
                tipoNc,        // Extra6 = TipoNC (descripción, ej. "Ajuste por Recepción")
                3);
        addendaRepository.save(addenda);
        log.info("Addenda Tipo 3 (NC) generada desde datos del portal: uuid={} idProveedor={} tipoNC={}",
                uuid, idProveedor, tipoNc);
    }

    /** Lee el texto del primer hijo con ese nombre dentro del nodo addenda (sin namespace). */
    private String addendaChild(Element parent, String name) {
        NodeList nl = parent.getElementsByTagName(name);
        if (nl.getLength() == 0) {
            return null;
        }
        String txt = nl.item(0).getTextContent();
        return (txt == null || txt.trim().isEmpty()) ? null : txt.trim();
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

    /**
     * Lee el valor de TipoNC (o TipoNotaCredito, variante de addenda) como elemento o
     * atributo. Regla MXSTM (Ivan 2026-07-31): TipoNC=2 (Descuento Comercial) no se
     * descarga. Devuelve null si el documento no trae addenda o no incluye el campo.
     */
    public String getTipoNotaCredito(String xmlContent) {
        try {
            Document doc = parseXml(xmlContent);
            NodeList addendas = doc.getElementsByTagNameNS(NS_CFDI, "Addenda");
            if (addendas.getLength() == 0) {
                addendas = doc.getElementsByTagName("cfdi:Addenda");
            }
            if (addendas.getLength() == 0) {
                return null;
            }
            String valor = getAddendaFieldValue(addendas.item(0), "TipoNC");
            if (valor == null) {
                valor = getAddendaFieldValue(addendas.item(0), "TipoNotaCredito");
            }
            return valor;
        } catch (Exception e) {
            log.warn("No se pudo leer TipoNC de la addenda: {}", e.getMessage());
            return null;
        }
    }

    private String getAddendaFieldValue(Node addendaNode, String fieldName) {
        if (addendaNode instanceof Element) {
            Element elem = (Element) addendaNode;
            NodeList children = elem.getElementsByTagName("*");
            for (int i = 0; i < children.getLength(); i++) {
                Element child = (Element) children.item(i);
                if ((child.getLocalName() != null && child.getLocalName().equalsIgnoreCase(fieldName))
                        || child.getTagName().endsWith(":" + fieldName)) {
                    String text = child.getTextContent();
                    if (text != null && !text.trim().isEmpty()) {
                        return text.trim();
                    }
                }
                if (child.hasAttribute(fieldName) && !child.getAttribute(fieldName).isEmpty()) {
                    return child.getAttribute(fieldName).trim();
                }
            }
        }
        return null;
    }

    // ── Utilidades ──────────────────────────────────────────────

    private Document parseXml(String xmlContent) throws Exception {
        // Strip BOM (UTF-8 \uFEFF) que algunos proveedores incluyen en el XML
        if (xmlContent != null && xmlContent.startsWith("\uFEFF")) {
            xmlContent = xmlContent.substring(1);
        }
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

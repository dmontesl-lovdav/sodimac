package com.sodimac.fiscal.api.service.impl;

import java.io.IOException;
import java.io.StringReader;
import java.util.ArrayList;

import javax.xml.XMLConstants;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.stream.StreamSource;
import javax.xml.validation.Schema;
import javax.xml.validation.SchemaFactory;
import javax.xml.validation.Validator;

import org.springframework.stereotype.Service;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

import com.sodimac.fiscal.api.model.dto.ParsedPaymentXmlDto;
import com.sodimac.fiscal.api.model.dto.invoicexml.DoctoRelacionadoDto;
import com.sodimac.fiscal.api.model.dto.invoicexml.PagoDto;
import com.sodimac.fiscal.api.model.dto.invoicexml.PagosDto;
import com.sodimac.fiscal.api.model.dto.invoicexml.TimbreFiscalDigitalDto;
import com.sodimac.fiscal.api.model.enums.FiscalMessageCode;
import com.sodimac.fiscal.api.service.MessageCatalogService;
import com.sodimac.fiscal.api.service.PaymentXmlParserService;
import com.sodimac.fiscal.api.util.XmlSecureFactory;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * Implementación del servicio de parseo de XMLs de complementos de pago.
 *
 * @author Sodimac Tech Team
 * @version 1.0
 * @since 2025
 */
@Service
@Slf4j
@RequiredArgsConstructor
public class PaymentXmlParserServiceImpl implements PaymentXmlParserService {

    private final MessageCatalogService messageCatalog;

    @Override
    public ParsedPaymentXmlDto parsePaymentXml(String xmlContent) {
        log.debug("Parseando XML de complemento de pago desde String");
        Document document = xmlStringToDocument(xmlContent);
        return parsePaymentXml(document);
    }

    @Override
    public ParsedPaymentXmlDto parsePaymentXml(Document document) {
        log.debug("Parseando XML de complemento de pago desde Document");

        try {
            ParsedPaymentXmlDto parsed = ParsedPaymentXmlDto.builder().build();

            Element root = document.getDocumentElement();

            // Parsear atributos del Comprobante
            parseComprobanteAttributes(root, parsed);

            // Parsear Emisor
            parseEmisor(root, parsed);

            // Parsear Receptor
            parseReceptor(root, parsed);

            // Parsear Complemento
            parseComplemento(root, parsed);

            // Parsear Addenda si existe
            parseAddenda(root, parsed);

            log.info("XML de complemento de pago parseado exitosamente");
            return parsed;

        } catch (Exception e) {
            log.error("Error parseando XML de complemento de pago", e);
            messageCatalog.throwError(FiscalMessageCode.ERR004, e.getMessage(), e);
        }
        return null; // Nunca alcanza aquí por el throw anterior
    }
    
    @Override
    public boolean validateXmlStructure(String xmlContent) {
        log.debug("Validando estructura XML contra esquemas XSD del SAT (CFDI 4.0, Pagos 2.0, TFD 1.1)");

        try {
            SchemaFactory schemaFactory = SchemaFactory.newInstance(XMLConstants.W3C_XML_SCHEMA_NS_URI);

            // --- INICIO DE MODIFICACIÓN ---

            // 1. Define todos los esquemas XSD *principales* que el XML de complemento.
            //    Estos son los que se declaran en el 'xsi:schemaLocation' del XML.
            String[] schemaFiles = {
                "/xsd/cfdv40.xsd",
                "/xsd/Pagos20.xsd",
                "/xsd/TimbreFiscalDigitalv11.xsd"
            };

            // 2. Crea un array de StreamSource.
            java.util.List<StreamSource> xsdSources = new java.util.ArrayList<>();
            for (String xsdFile : schemaFiles) {
                // Obtenemos la URL del recurso
                java.net.URL xsdUrl = getClass().getResource(xsdFile);
                if (xsdUrl == null) {
                    log.error("No se encontró el archivo XSD requerido en resources: {}", xsdFile);
                    throw new IOException("Falta el archivo de esquema requerido: " + xsdFile);
                }

                StreamSource streamSource = new StreamSource(xsdUrl.openStream());
                // Asignamos el 'systemId' para que el parser sepa la "dirección"
                // de este XSD y pueda resolver importaciones relativas.
                streamSource.setSystemId(xsdUrl.toExternalForm()); 
                
                xsdSources.add(streamSource);
            }

            if (xsdSources.isEmpty()) {
                 log.warn("No se cargaron esquemas XSD, omitiendo validación.");
                 return true;
            }

            // 3. Crea el Schema a partir del *array* de fuentes
            Schema schema = schemaFactory.newSchema(xsdSources.toArray(new StreamSource[0]));
            
            Validator validator = schema.newValidator();

            // Validamos el XML
            validator.validate(new StreamSource(new StringReader(xmlContent)));

            // Actualizamos el log de éxito
            log.debug("XML válido según esquemas del SAT (CFDI 4.0, Pagos 2.0, TFD 1.1)");
            return true;

        } catch (SAXException e) {
            // Es buena idea loguear la excepción completa para más detalles
            log.error("XML no cumple con el esquema XSD: {}", e.getMessage(), e);
            return false;
        } catch (IOException e) {
            log.error("Error leyendo XML o XSD para validación: {}", e.getMessage(), e);
            return false;
        }
    }


    @Override
    public Document xmlStringToDocument(String xmlContent) {
        try {
            DocumentBuilderFactory factory = XmlSecureFactory.newDocumentBuilderFactory();
            factory.setNamespaceAware(true);
            factory.setIgnoringElementContentWhitespace(true);

            DocumentBuilder builder = factory.newDocumentBuilder();
            InputSource inputSource = new InputSource(new StringReader(xmlContent));

            return builder.parse(inputSource);

        } catch (ParserConfigurationException | SAXException | IOException e) {
            log.error("Error convirtiendo XML String a Document", e);
            messageCatalog.throwError(FiscalMessageCode.ERR027, e.getMessage(), e);
        }
        return null; // Nunca alcanza aquí por el throw anterior
    }

    // ========== Métodos privados de parseo ==========

    private void parseComprobanteAttributes(Element root, ParsedPaymentXmlDto parsed) {
        parsed.setVersion(root.getAttribute("Version"));
        parsed.setSerie(root.getAttribute("Serie"));
        parsed.setFolio(root.getAttribute("Folio"));
        parsed.setFecha(root.getAttribute("Fecha"));
        parsed.setSello(root.getAttribute("Sello"));
        parsed.setNoCertificado(root.getAttribute("NoCertificado"));
        parsed.setCertificado(root.getAttribute("Certificado"));
        parsed.setSubTotal(root.getAttribute("SubTotal"));
        parsed.setMoneda(root.getAttribute("Moneda"));
        parsed.setTotal(root.getAttribute("Total"));
        parsed.setTipoDeComprobante(root.getAttribute("TipoDeComprobante"));
        parsed.setExportacion(root.getAttribute("Exportacion"));
        parsed.setLugarExpedicion(root.getAttribute("LugarExpedicion"));

        log.debug("Comprobante parseado: Folio={}, Serie={}, TipoComprobante={}",
                parsed.getFolio(), parsed.getSerie(), parsed.getTipoDeComprobante());
    }

    private void parseEmisor(Element root, ParsedPaymentXmlDto parsed) {
        NodeList emisorNodes = root.getElementsByTagNameNS("*", "Emisor");
        if (emisorNodes.getLength() > 0) {
            Element emisor = (Element) emisorNodes.item(0);
            parsed.setEmisorRfc(emisor.getAttribute("Rfc"));
            parsed.setEmisorNombre(emisor.getAttribute("Nombre"));
            parsed.setEmisorRegimenFiscal(emisor.getAttribute("RegimenFiscal"));

            log.debug("Emisor parseado: RFC={}", parsed.getEmisorRfc());
        }
    }

    private void parseReceptor(Element root, ParsedPaymentXmlDto parsed) {
        NodeList receptorNodes = root.getElementsByTagNameNS("*", "Receptor");
        if (receptorNodes.getLength() > 0) {
            Element receptor = (Element) receptorNodes.item(0);
            parsed.setReceptorRfc(receptor.getAttribute("Rfc"));
            parsed.setReceptorNombre(receptor.getAttribute("Nombre"));
            parsed.setReceptorDomicilioFiscal(receptor.getAttribute("DomicilioFiscalReceptor"));
            parsed.setReceptorRegimenFiscal(receptor.getAttribute("RegimenFiscalReceptor"));
            parsed.setReceptorUsoCFDI(receptor.getAttribute("UsoCFDI"));

            log.debug("Receptor parseado: RFC={}", parsed.getReceptorRfc());
        }
    }

    private void parseComplemento(Element root, ParsedPaymentXmlDto parsed) {
        NodeList complementoNodes = root.getElementsByTagNameNS("*", "Complemento");
        if (complementoNodes.getLength() == 0) {
            messageCatalog.throwError(FiscalMessageCode.ERR013);
        }

        Element complemento = (Element) complementoNodes.item(0);

        // Parsear TimbreFiscalDigital
        parseTimbreFiscalDigital(complemento, parsed);

        // Parsear Pagos
        parsePagos(complemento, parsed);
    }

    private void parseTimbreFiscalDigital(Element complemento, ParsedPaymentXmlDto parsed) {
        NodeList timbreNodes = complemento.getElementsByTagNameNS("*", "TimbreFiscalDigital");
        if (timbreNodes.getLength() > 0) {
            Element timbre = (Element) timbreNodes.item(0);

            TimbreFiscalDigitalDto timbreDto = new TimbreFiscalDigitalDto();
            timbreDto.setVersion(timbre.getAttribute("Version"));
            timbreDto.setUuid(timbre.getAttribute("UUID"));
            timbreDto.setFechaTimbrado(timbre.getAttribute("FechaTimbrado"));
            timbreDto.setRfcProvCertif(timbre.getAttribute("RfcProvCertif"));
            timbreDto.setSelloCFD(timbre.getAttribute("SelloCFD"));
            timbreDto.setNoCertificadoSAT(timbre.getAttribute("NoCertificadoSAT"));
            timbreDto.setSelloSAT(timbre.getAttribute("SelloSAT"));

            parsed.setTimbreFiscalDigital(timbreDto);

            log.debug("Timbre Fiscal Digital parseado: UUID={}", timbreDto.getUuid());
        }
    }

    private void parsePagos(Element complemento, ParsedPaymentXmlDto parsed) {
        String ns = "http://www.sat.gob.mx/Pagos20";
        NodeList pagosNodes = complemento.getElementsByTagNameNS(ns, "Pagos");
        if (pagosNodes.getLength() == 0) {
            messageCatalog.throwError(FiscalMessageCode.ERR024);
        }

        Element pagosElement = (Element) pagosNodes.item(0);
        PagosDto pagosDto = new PagosDto();

        // Parsear cada nodo <pago20:Pago>
        NodeList pagoNodes = pagosElement.getElementsByTagNameNS(ns, "Pago");
        ArrayList<PagoDto> pagos = new ArrayList<>();

        for (int i = 0; i < pagoNodes.getLength(); i++) {
            Element pagoEl = (Element) pagoNodes.item(i);
            PagoDto pago = new PagoDto();
            pago.setFechaPago(pagoEl.getAttribute("FechaPago"));
            pago.setFormaDePagoP(pagoEl.getAttribute("FormaDePagoP"));
            pago.setMonedaP(pagoEl.getAttribute("MonedaP"));
            pago.setTipoCambioP(pagoEl.getAttribute("TipoCambioP"));
            pago.setMonto(pagoEl.getAttribute("Monto"));
            pago.setNumOperacion(pagoEl.getAttribute("NumOperacion"));
            pago.setRfcEmisorCtaOrd(pagoEl.getAttribute("RfcEmisorCtaOrd"));
            pago.setNomBancoOrdExt(pagoEl.getAttribute("NomBancoOrdExt"));
            pago.setCtaOrdenante(pagoEl.getAttribute("CtaOrdenante"));
            pago.setRfcEmisorCtaBen(pagoEl.getAttribute("RfcEmisorCtaBen"));
            pago.setCtaBeneficiario(pagoEl.getAttribute("CtaBeneficiario"));

            // Parsear cada nodo <pago20:DoctoRelacionado>
            NodeList doctoNodes = pagoEl.getElementsByTagNameNS(ns, "DoctoRelacionado");
            ArrayList<DoctoRelacionadoDto> doctos = new ArrayList<>();

            for (int j = 0; j < doctoNodes.getLength(); j++) {
                Element doctoEl = (Element) doctoNodes.item(j);
                DoctoRelacionadoDto docto = new DoctoRelacionadoDto();
                docto.setIdDocumento(doctoEl.getAttribute("IdDocumento"));
                docto.setSerie(doctoEl.getAttribute("Serie"));
                docto.setFolio(doctoEl.getAttribute("Folio"));
                docto.setMonedaDR(doctoEl.getAttribute("MonedaDR"));
                docto.setEquivalenciaDR(doctoEl.getAttribute("EquivalenciaDR"));
                docto.setNumParcialidad(doctoEl.getAttribute("NumParcialidad"));
                docto.setImpSaldoAnt(doctoEl.getAttribute("ImpSaldoAnt"));
                docto.setImpPagado(doctoEl.getAttribute("ImpPagado"));
                docto.setImpSaldoInsoluto(doctoEl.getAttribute("ImpSaldoInsoluto"));
                docto.setObjetoImpDR(doctoEl.getAttribute("ObjetoImpDR"));
                doctos.add(docto);
            }
            pago.setDoctosRelacionados(doctos);
            pagos.add(pago);
        }

        pagosDto.setPagos(pagos);
        parsed.setPagos(pagosDto);

        log.debug("Pagos parseados: {} pagos con documentos relacionados", pagos.size());
    }

    private void parseAddenda(Element root, ParsedPaymentXmlDto parsed) {
        NodeList addendaNodes = root.getElementsByTagNameNS("*", "Addenda");
        if (addendaNodes.getLength() > 0) {
            Element addenda = (Element) addendaNodes.item(0);
            // Convertir el nodo a String XML
            parsed.setAddendaContent(nodeToString(addenda));

            log.debug("Addenda encontrada y parseada");
        }
    }

    private String nodeToString(Element node) {
        try {
            javax.xml.transform.TransformerFactory tf = XmlSecureFactory.newTransformerFactory();
            javax.xml.transform.Transformer transformer = tf.newTransformer();
            transformer.setOutputProperty(javax.xml.transform.OutputKeys.OMIT_XML_DECLARATION, "yes");
            java.io.StringWriter writer = new java.io.StringWriter();
            transformer.transform(new javax.xml.transform.dom.DOMSource(node), new javax.xml.transform.stream.StreamResult(writer));
            return writer.toString();
        } catch (Exception e) {
            log.warn("Error convirtiendo nodo a String", e);
            return "";
        }
    }
}

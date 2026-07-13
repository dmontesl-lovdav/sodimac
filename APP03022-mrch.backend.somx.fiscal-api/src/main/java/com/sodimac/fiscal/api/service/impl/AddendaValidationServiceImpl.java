package com.sodimac.fiscal.api.service.impl;

import com.sodimac.fiscal.api.exception.FiscalException;
import com.sodimac.fiscal.api.model.dto.invoicexml.InvoiceXmlDto;
import com.sodimac.fiscal.api.model.enums.FiscalMessageCode;
import com.sodimac.fiscal.api.service.AddendaValidationService;
import com.sodimac.fiscal.api.service.MessageCatalogService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.InputSource;

import com.sodimac.fiscal.api.util.XmlSecureFactory;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import java.io.StringReader;
import java.util.regex.Pattern;

/**
 * Implementación del servicio para validar la estructura y contenido de addendas en facturas y notas de crédito.
 *
 * Valida que las addendas cumplan con los requisitos de Sodimac:
 * - Addenda_Sodimac para documentos de mercancía, servicios y transporte local
 * - Addenda_Sodimac_CartaPorte para transporte foráneo
 * - Campos obligatorios: RFC, UUID, Folio, NoOC, Proveedor
 *
 * @author Sodimac Tech Team
 * @since 2025-11-10
 */
@Service
@Slf4j
@RequiredArgsConstructor
public class AddendaValidationServiceImpl implements AddendaValidationService {

    private final MessageCatalogService messageCatalog;

    private static final Pattern RFC_PATTERN = Pattern.compile("^[A-ZÑ&]{3,4}\\d{6}[A-Z0-9]{3}$");
    private static final Pattern UUID_PATTERN = Pattern.compile("^[0-9a-fA-F]{8}-[0-9a-fA-F]{4}-[0-9a-fA-F]{4}-[0-9a-fA-F]{4}-[0-9a-fA-F]{12}$");

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean validateAddenda(String xmlContent, InvoiceXmlDto invoiceDto) {
        log.info("Iniciando validación de addenda para documento Serie: {}, Folio: {}",
                invoiceDto.getSerie(), invoiceDto.getFolio());

        try {
            // Parsear XML para acceder al nodo Addenda
            DocumentBuilderFactory factory = XmlSecureFactory.newDocumentBuilderFactory();
            factory.setNamespaceAware(true);
            DocumentBuilder builder = factory.newDocumentBuilder();
            Document doc = builder.parse(new InputSource(new StringReader(xmlContent)));

            // Buscar nodo Addenda
            NodeList addendaNodes = doc.getElementsByTagName("cfdi:Addenda");
            if (addendaNodes.getLength() == 0) {
                addendaNodes = doc.getElementsByTagName("Addenda");
            }

            if (addendaNodes.getLength() == 0) {
                log.info("No se encontró nodo Addenda en el documento. Será marcado como pendiente de addenda");
                return false; // No tiene addenda, pero no es error
            }

            Element addendaElement = (Element) addendaNodes.item(0);
            log.debug("Nodo Addenda encontrado. Validando estructura...");

            // Buscar nodo de addenda Sodimac reconocido
            Element addendaSodimac = findAddendaSodimacNode(addendaElement);
            if (addendaSodimac == null) {
                log.error("La addenda no contiene un nodo Sodimac reconocido");
                messageCatalog.throwException(FiscalMessageCode.BUS001);
                return false;
            }

            String addendaType = addendaSodimac.getLocalName();
            log.debug("Tipo de addenda encontrada: {}", addendaType);

            // Validar campos obligatorios según el tipo
            if ("Addenda_Sodimac".equals(addendaType)) {
                validateAddendaSodimac(addendaSodimac, invoiceDto);
            } else if ("Addenda_Sodimac_CartaPorte".equals(addendaType)) {
                validateAddendaSodimacCartaPorte(addendaSodimac, invoiceDto);
            } else if ("Addenda_Transportistas_Sodimac_Detecno".equals(addendaType)) {
                validateAddendaTransportistas(addendaSodimac);
            }

            log.info("Addenda validada exitosamente para documento Serie: {}, Folio: {}",
                    invoiceDto.getSerie(), invoiceDto.getFolio());
            return true;

        } catch (FiscalException e) {
            throw e; // Re-lanzar excepciones de negocio
        } catch (Exception e) {
            log.error("Error técnico procesando addenda del documento", e);
            messageCatalog.throwException(FiscalMessageCode.BUS001, e.getMessage(), e);
        }
        return false; // Nunca alcanza aquí por los throws anteriores
    }

    /**
     * Busca el nodo de addenda Sodimac reconocido dentro del elemento Addenda.
     * Tipos soportados:
     *   - Addenda_Sodimac (mercancia, servicios, transporte local)
     *   - Addenda_Sodimac_CartaPorte (transporte foraneo)
     *   - Addenda_Transportistas_Sodimac_Detecno (transportistas Detecno)
     */
    private Element findAddendaSodimacNode(Element addendaElement) {
        log.debug("Buscando nodo de addenda Sodimac reconocido...");

        NodeList children = addendaElement.getChildNodes();
        for (int i = 0; i < children.getLength(); i++) {
            Node child = children.item(i);
            if (child.getNodeType() == Node.ELEMENT_NODE) {
                Element element = (Element) child;
                String localName = element.getLocalName();
                if ("Addenda_Sodimac".equals(localName)
                        || "Addenda_Sodimac_CartaPorte".equals(localName)
                        || "Addenda_Transportistas_Sodimac_Detecno".equals(localName)) {
                    log.debug("Nodo {} encontrado", localName);
                    return element;
                }
            }
        }

        log.warn("No se encontró nodo de addenda Sodimac reconocido");
        return null;
    }

    /**
     * Valida la estructura de Addenda_Transportistas_Sodimac_Detecno.
     * Campos obligatorios: IdProveedor, IdGuiaEntrega, IdViaje.
     */
    private void validateAddendaTransportistas(Element addenda) {
        log.debug("Validando estructura de Addenda_Transportistas_Sodimac_Detecno...");

        String idProveedor = getChildText(addenda, "IdProveedor");
        if (idProveedor == null || idProveedor.trim().isEmpty()) {
            log.error("Campo IdProveedor faltante en Addenda_Transportistas_Sodimac_Detecno");
            messageCatalog.throwException(FiscalMessageCode.BUS017);
        }

        String idGuia = getChildText(addenda, "IdGuiaEntrega");
        if (idGuia == null || idGuia.trim().isEmpty()) {
            log.error("Campo IdGuiaEntrega faltante en Addenda_Transportistas_Sodimac_Detecno");
            messageCatalog.throwException(FiscalMessageCode.BUS016);
        }

        String idViaje = getChildText(addenda, "IdViaje");
        if (idViaje == null || idViaje.trim().isEmpty()) {
            log.error("Campo IdViaje faltante en Addenda_Transportistas_Sodimac_Detecno");
            messageCatalog.throwException(FiscalMessageCode.BUS001, "Campo IdViaje es obligatorio");
        }

        log.info("Addenda_Transportistas_Sodimac_Detecno validada — IdProveedor: {}, IdGuia: {}, IdViaje: {}",
                idProveedor, idGuia, idViaje);
    }

    /**
     * Obtiene el texto del primer hijo con el nombre dado.
     */
    private String getChildText(Element parent, String childName) {
        NodeList nodes = parent.getElementsByTagName(childName);
        if (nodes.getLength() == 0) return null;
        return nodes.item(0).getTextContent();
    }

    /**
     * Valida la estructura de Addenda_Sodimac (mercancía, servicios, transporte local).
     */
    private void validateAddendaSodimac(Element addendaSodimac, InvoiceXmlDto invoiceDto) {
        log.debug("Validando estructura de Addenda_Sodimac...");

        // Validar campo RFC
        String rfc = addendaSodimac.getAttribute("RFC");
        if (rfc == null || rfc.trim().isEmpty()) {
            log.error("Campo RFC faltante en Addenda_Sodimac");
            messageCatalog.throwException(FiscalMessageCode.BUS013);
        }
        if (!RFC_PATTERN.matcher(rfc.trim()).matches()) {
            log.error("Formato de RFC inválido en addenda: {}", rfc);
            messageCatalog.throwException(FiscalMessageCode.BUS018, rfc);
        }
        log.debug("Campo RFC válido: {}", rfc);

        // Validar campo UUID
        String uuid = addendaSodimac.getAttribute("UUID");
        if (uuid == null || uuid.trim().isEmpty()) {
            log.error("Campo UUID faltante en Addenda_Sodimac");
            messageCatalog.throwException(FiscalMessageCode.BUS014);
        }
        if (!UUID_PATTERN.matcher(uuid.trim()).matches()) {
            log.error("Formato de UUID inválido en addenda: {}", uuid);
            messageCatalog.throwException(FiscalMessageCode.BUS001, "UUID inválido: " + uuid);
        }

        // Validar que UUID de addenda coincida con UUID del TimbreFiscalDigital
        String uuidTimbrado = invoiceDto.getTimbreFiscalDigital() != null
                ? invoiceDto.getTimbreFiscalDigital().getUuid()
                : null;

        if (uuidTimbrado != null && !uuid.equalsIgnoreCase(uuidTimbrado)) {
            log.error("UUID de addenda ({}) no coincide con UUID de TimbreFiscalDigital ({})", uuid, uuidTimbrado);
            messageCatalog.throwException(FiscalMessageCode.BUS019);
        }
        log.debug("Campo UUID válido y coincidente: {}", uuid);

        // Validar campo Folio
        String folio = addendaSodimac.getAttribute("Folio");
        if (folio == null || folio.trim().isEmpty()) {
            log.error("Campo Folio faltante en Addenda_Sodimac");
            messageCatalog.throwException(FiscalMessageCode.BUS015);
        }
        log.debug("Campo Folio válido: {}", folio);

        // Validar campo NoOC (Número de Orden de Compra)
        String noOC = addendaSodimac.getAttribute("NoOC");
        if (noOC == null || noOC.trim().isEmpty()) {
            log.error("Campo NoOC (Número de Orden de Compra) faltante en Addenda_Sodimac");
            messageCatalog.throwException(FiscalMessageCode.BUS016);
        }
        log.debug("Campo NoOC válido: {}", noOC);

        // Validar campo Proveedor
        String proveedor = addendaSodimac.getAttribute("Proveedor");
        if (proveedor == null || proveedor.trim().isEmpty()) {
            log.error("Campo Proveedor faltante en Addenda_Sodimac");
            messageCatalog.throwException(FiscalMessageCode.BUS017);
        }
        log.debug("Campo Proveedor válido: {}", proveedor);

        log.info("Addenda_Sodimac validada exitosamente - RFC: {}, UUID: {}, Folio: {}, NoOC: {}",
                rfc, uuid, folio, noOC);
    }

    /**
     * Valida la estructura de Addenda_Sodimac_CartaPorte (transporte foráneo).
     */
    private void validateAddendaSodimacCartaPorte(Element addendaCartaPorte, InvoiceXmlDto invoiceDto) {
        log.debug("Validando estructura de Addenda_Sodimac_CartaPorte...");

        // Validar campo RFC
        String rfc = addendaCartaPorte.getAttribute("RFC");
        if (rfc == null || rfc.trim().isEmpty()) {
            log.error("Campo RFC faltante en Addenda_Sodimac_CartaPorte");
            messageCatalog.throwException(FiscalMessageCode.BUS013);
        }
        if (!RFC_PATTERN.matcher(rfc.trim()).matches()) {
            log.error("Formato de RFC inválido en addenda CartaPorte: {}", rfc);
            messageCatalog.throwException(FiscalMessageCode.BUS018, rfc);
        }
        log.debug("Campo RFC válido: {}", rfc);

        // Validar campo UUID
        String uuid = addendaCartaPorte.getAttribute("UUID");
        if (uuid == null || uuid.trim().isEmpty()) {
            log.error("Campo UUID faltante en Addenda_Sodimac_CartaPorte");
            messageCatalog.throwException(FiscalMessageCode.BUS014);
        }
        if (!UUID_PATTERN.matcher(uuid.trim()).matches()) {
            log.error("Formato de UUID inválido en addenda CartaPorte: {}", uuid);
            messageCatalog.throwException(FiscalMessageCode.BUS001, "UUID inválido: " + uuid);
        }

        // Validar que UUID de addenda coincida con UUID del TimbreFiscalDigital
        String uuidTimbrado = invoiceDto.getTimbreFiscalDigital() != null
                ? invoiceDto.getTimbreFiscalDigital().getUuid()
                : null;

        if (uuidTimbrado != null && !uuid.equalsIgnoreCase(uuidTimbrado)) {
            log.error("UUID de addenda CartaPorte ({}) no coincide con UUID de TimbreFiscalDigital ({})",
                    uuid, uuidTimbrado);
            messageCatalog.throwException(FiscalMessageCode.BUS019);
        }
        log.debug("Campo UUID válido y coincidente: {}", uuid);

        // Validar campo Folio
        String folio = addendaCartaPorte.getAttribute("Folio");
        if (folio == null || folio.trim().isEmpty()) {
            log.error("Campo Folio faltante en Addenda_Sodimac_CartaPorte");
            messageCatalog.throwException(FiscalMessageCode.BUS015);
        }
        log.debug("Campo Folio válido: {}", folio);

        // Validar campo NoOC (Número de Orden de Compra)
        String noOC = addendaCartaPorte.getAttribute("NoOC");
        if (noOC == null || noOC.trim().isEmpty()) {
            log.error("Campo NoOC faltante en Addenda_Sodimac_CartaPorte");
            messageCatalog.throwException(FiscalMessageCode.BUS016);
        }
        log.debug("Campo NoOC válido: {}", noOC);

        // Validar campo Proveedor
        String proveedor = addendaCartaPorte.getAttribute("Proveedor");
        if (proveedor == null || proveedor.trim().isEmpty()) {
            log.error("Campo Proveedor faltante en Addenda_Sodimac_CartaPorte");
            messageCatalog.throwException(FiscalMessageCode.BUS017);
        }
        log.debug("Campo Proveedor válido: {}", proveedor);

        log.info("Addenda_Sodimac_CartaPorte validada exitosamente - RFC: {}, UUID: {}, Folio: {}, NoOC: {}",
                rfc, uuid, folio, noOC);
    }
}

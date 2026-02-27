package com.sodimac.fiscal.api.service.impl;

import com.sodimac.fiscal.api.model.dto.NCValidationRequest;
import com.sodimac.fiscal.api.model.dto.NCValidationResponse;
import com.sodimac.fiscal.api.model.enums.FiscalMessageCode;
import com.sodimac.fiscal.api.service.MessageCatalogService;
import com.sodimac.fiscal.api.service.NCValidationService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/**
 * ============================================================================
 * SERVICE IMPL: NCValidationServiceImpl
 * ============================================================================
 * Implementación del servicio de validación de NC.
 *
 * LÓGICA:
 * 1. Parsear XML de NC
 * 2. Extraer UUID fiscal de NC (TimbreFiscalDigital)
 * 3. Buscar nodo CfdiRelacionados
 * 4. Validar que contiene el UUID de la factura
 * 5. Retornar resultado (NO GUARDA NADA)
 *
 * @author Sodimac Tech Team
 * @version 1.0
 * @since 2025
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class NCValidationServiceImpl implements NCValidationService {

    private static final String CFDI_NAMESPACE = "http://www.sat.gob.mx/cfd/4";
    private static final String TFD_NAMESPACE = "http://www.sat.gob.mx/TimbreFiscalDigital";

    private final MessageCatalogService messageCatalogService;

    @Override
    public NCValidationResponse validateNCRelation(NCValidationRequest request, MultipartFile xmlFile) {
        log.info("Validando relación NC con factura: {}", request.getInvoiceFiscalUuid());

        // 1. Parsear XML
        Document xmlDocument = parseXML(xmlFile);
        if (xmlDocument == null) {
            messageCatalogService.throwError(FiscalMessageCode.ERR004);
        }

        // 2. Extraer UUID fiscal de NC (TimbreFiscalDigital)
        UUID ncFiscalUuid = extractNCFiscalUuid(xmlDocument);
        if (ncFiscalUuid == null) {
            messageCatalogService.throwError(FiscalMessageCode.BUS028);
        }

        // 3. Validar que es tipo Egreso (Nota de Crédito)
        String tipoComprobante = xmlDocument.getDocumentElement().getAttribute("TipoDeComprobante");
        if (!"E".equals(tipoComprobante)) {
            messageCatalogService.throwError(FiscalMessageCode.ERR018);
        }

        // 4. Buscar CfdiRelacionados
        NodeList relacionadosList = xmlDocument.getElementsByTagNameNS(CFDI_NAMESPACE, "CfdiRelacionados");
        if (relacionadosList.getLength() == 0) {
            messageCatalogService.throwError(FiscalMessageCode.BUS042);
        }

        // 5. Extraer tipo de relación y UUIDs relacionados
        Element cfdiRelacionados = (Element) relacionadosList.item(0);
        String relationshipType = cfdiRelacionados.getAttribute("TipoRelacion");

        List<UUID> relatedUuids = extractRelatedUuids(xmlDocument);

        // 6. Validar que el UUID de la factura está en la lista
        if (!relatedUuids.contains(request.getInvoiceFiscalUuid())) {
            messageCatalogService.throwError(FiscalMessageCode.BUS043);
        }

        // 7. Validación exitosa
        log.info("Validación exitosa: NC {} relacionada con factura {}",
                ncFiscalUuid, request.getInvoiceFiscalUuid());

        return NCValidationResponse.success(ncFiscalUuid, relatedUuids, relationshipType);
    }

    /**
     * Parsea el archivo XML a Document
     */
    private Document parseXML(MultipartFile xmlFile) {
        try (InputStream inputStream = xmlFile.getInputStream()) {
            DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
            factory.setNamespaceAware(true);
            DocumentBuilder builder = factory.newDocumentBuilder();
            return builder.parse(inputStream);
        } catch (Exception e) {
            log.error("Error al parsear XML", e);
            return null;
        }
    }

    /**
     * Extrae el UUID fiscal de la NC desde TimbreFiscalDigital
     */
    private UUID extractNCFiscalUuid(Document xmlDocument) {
        try {
            NodeList tfdList = xmlDocument.getElementsByTagNameNS(TFD_NAMESPACE, "TimbreFiscalDigital");
            if (tfdList.getLength() > 0) {
                Element tfd = (Element) tfdList.item(0);
                String uuidStr = tfd.getAttribute("UUID");
                return UUID.fromString(uuidStr);
            }
        } catch (Exception e) {
            log.error("Error al extraer UUID fiscal de NC", e);
        }
        return null;
    }

    /**
     * Extrae todos los UUIDs relacionados del XML
     */
    private List<UUID> extractRelatedUuids(Document xmlDocument) {
        List<UUID> uuids = new ArrayList<>();
        try {
            NodeList cfdiRelacionadoList = xmlDocument.getElementsByTagNameNS(
                    CFDI_NAMESPACE, "CfdiRelacionado");

            for (int i = 0; i < cfdiRelacionadoList.getLength(); i++) {
                Element cfdiRelacionado = (Element) cfdiRelacionadoList.item(i);
                String uuidStr = cfdiRelacionado.getAttribute("UUID");
                uuids.add(UUID.fromString(uuidStr));
            }
        } catch (Exception e) {
            log.error("Error al extraer UUIDs relacionados", e);
        }
        return uuids;
    }
}

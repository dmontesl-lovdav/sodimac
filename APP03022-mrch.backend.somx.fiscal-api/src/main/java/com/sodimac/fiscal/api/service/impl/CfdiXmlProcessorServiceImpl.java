package com.sodimac.fiscal.api.service.impl;

import com.sodimac.fiscal.api.model.dto.invoicexml.CartaPorteDto;
import com.sodimac.fiscal.api.model.dto.invoicexml.ComplementoDto;
import com.sodimac.fiscal.api.model.dto.invoicexml.InvoiceXmlDto;
import com.sodimac.fiscal.api.model.enums.FiscalMessageCode;
import com.sodimac.fiscal.api.model.enums.TipoDocumentoFiscal;
import com.sodimac.fiscal.api.service.CfdiXmlProcessorService;
import com.sodimac.fiscal.api.service.MessageCatalogService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import jakarta.xml.bind.JAXBContext;
import jakarta.xml.bind.JAXBException;
import jakarta.xml.bind.Unmarshaller;
import java.io.StringReader;

/**
 * Implementación del servicio especializado para procesar documentos CFDI v4.0.
 *
 * Maneja facturas (TipoDocumento="I") y notas de crédito (TipoDocumento="E")
 * utilizando el XSD cfdv40.xsd del SAT mexicano.
 *
 * @author g_dco018
 * @version 1.0
 * @since 2025
 */
@Service
@Slf4j
public class CfdiXmlProcessorServiceImpl implements CfdiXmlProcessorService {

    private final MessageCatalogService messageCatalog;
    private JAXBContext jaxbContext;

    public CfdiXmlProcessorServiceImpl(MessageCatalogService messageCatalog) {
        this.messageCatalog = messageCatalog;
        try {
            this.jaxbContext = JAXBContext.newInstance(InvoiceXmlDto.class);
            log.info("JAXB Context inicializado para CFDI v4.0");
        } catch (JAXBException e) {
            log.error("Error inicializando JAXB Context para CFDI", e);
            messageCatalog.throwError(FiscalMessageCode.ERR009, e.getMessage(), e);
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public InvoiceXmlDto processCfdi(String xmlContent, TipoDocumentoFiscal tipoDocumento) {
        log.debug("Procesando CFDI v4.0: {}", tipoDocumento.getDescripcion());

        try {
            Unmarshaller unmarshaller = jaxbContext.createUnmarshaller();
            InvoiceXmlDto invoice = (InvoiceXmlDto) unmarshaller.unmarshal(new StringReader(xmlContent));

            // Validaciones específicas por tipo
            validateCfdiByType(invoice, tipoDocumento);

            // Log de complemento CartaPorte si existe
            logCartaPorteInfo(invoice);

            log.info("CFDI procesado exitosamente. Serie: {}, Folio: {}, Total: {}",
                    invoice.getSerie(), invoice.getFolio(), invoice.getTotal());

            return invoice;

        } catch (JAXBException e) {
            log.error("Error unmarshalling CFDI XML", e);
            messageCatalog.throwError(FiscalMessageCode.ERR012, e.getMessage(), e);
        }
        return null; // Nunca alcanza aquí
    }

    /**
     * Realiza validaciones específicas según el tipo de CFDI.
     *
     * @param invoice DTO del comprobante
     * @param tipoDocumento Tipo de documento esperado
     */
    private void validateCfdiByType(InvoiceXmlDto invoice, TipoDocumentoFiscal tipoDocumento) {
        String tipoComprobante = invoice.getTipoDeComprobante();

        switch (tipoDocumento) {
            case FACTURA:
                if (!"I".equals(tipoComprobante)) {
                    messageCatalog.throwError(FiscalMessageCode.ERR017,
                        String.format("Encontrado: %s", tipoComprobante));
                }
                validateFacturaRequired(invoice);
                break;

            case NOTA_CREDITO:
                if (!"E".equals(tipoComprobante)) {
                    messageCatalog.throwError(FiscalMessageCode.ERR018,
                        String.format("Encontrado: %s", tipoComprobante));
                }
                validateNotaCreditoRequired(invoice);
                break;

            default:
                messageCatalog.throwError(FiscalMessageCode.ERR016,
                    tipoDocumento.getDescripcion());
        }

        log.debug("Validaciones específicas completadas para: {}", tipoDocumento.getDescripcion());
    }

    /**
     * Valida campos requeridos específicos para Facturas.
     *
     * @param invoice DTO de la factura
     */
    private void validateFacturaRequired(InvoiceXmlDto invoice) {
        if (invoice.getConceptos() == null || invoice.getConceptos().isEmpty()) {
            messageCatalog.throwError(FiscalMessageCode.BUS037);
        }

        if (invoice.getTotal() == null || invoice.getTotal().isEmpty()) {
            messageCatalog.throwError(FiscalMessageCode.BUS038);
        }

        if (invoice.getReceptorRfc() == null || invoice.getReceptorRfc().isEmpty()) {
            messageCatalog.throwError(FiscalMessageCode.BUS010);
        }

        log.debug("Validación de Factura completada");
    }

    /**
     * Valida campos requeridos específicos para Notas de Crédito.
     *
     * @param invoice DTO de la nota de crédito
     */
    private void validateNotaCreditoRequired(InvoiceXmlDto invoice) {
        if (invoice.getConceptos() == null || invoice.getConceptos().isEmpty()) {
            messageCatalog.throwError(FiscalMessageCode.BUS037);
        }

        // Las notas de crédito pueden tener total negativo o positivo
        if (invoice.getTotal() == null || invoice.getTotal().isEmpty()) {
            messageCatalog.throwError(FiscalMessageCode.BUS038);
        }

        log.debug("Validación de Nota de Crédito completada");
    }

    /**
     * Registra informacion del complemento CartaPorte si existe.
     * Valida que la version sea soportada (3.0 o 3.1) y genera warning si no lo es.
     *
     * @param invoice DTO del comprobante
     */
    private void logCartaPorteInfo(InvoiceXmlDto invoice) {
        if (invoice.getComplemento() == null) {
            return;
        }

        ComplementoDto complemento = invoice.getComplemento();

        if (complemento.hasCartaPorte()) {
            String version = complemento.getCartaPorteVersion();
            CartaPorteDto cartaPorte = complemento.getCartaPorte();

            log.info("CartaPorte detectado - Version: {}, IdCCP: {}, TranspInternac: {}",
                    version,
                    cartaPorte.getIdCCP(),
                    cartaPorte.getTranspInternac());

            // Validar version soportada
            if (!"3.0".equals(version) && !"3.1".equals(version)) {
                log.warn("Version de CartaPorte no soportada: {}. Solo se soportan versiones 3.0 y 3.1", version);
            }
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String extractBasicInfo(String xmlContent) {
        try {
            InvoiceXmlDto invoice = processCfdi(xmlContent, TipoDocumentoFiscal.FACTURA);
            return String.format("CFDI - Serie: %s, Folio: %s, RFC Emisor: %s, Total: %s",
                    invoice.getSerie(),
                    invoice.getFolio(),
                    invoice.getEmisorRfc(),
                    invoice.getTotal());
        } catch (Exception e) {
            return "Error extrayendo información básica: " + e.getMessage();
        }
    }
}

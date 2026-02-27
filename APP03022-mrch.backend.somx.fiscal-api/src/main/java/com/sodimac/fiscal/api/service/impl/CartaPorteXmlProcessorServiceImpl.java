package com.sodimac.fiscal.api.service.impl;

import com.sodimac.fiscal.api.model.dto.invoicexml.CartaPorteDto;
import com.sodimac.fiscal.api.model.dto.invoicexml.InvoiceXmlDto;
import com.sodimac.fiscal.api.model.enums.FiscalMessageCode;
import com.sodimac.fiscal.api.service.CartaPorteXmlProcessorService;
import com.sodimac.fiscal.api.service.MessageCatalogService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import jakarta.xml.bind.JAXBContext;
import jakarta.xml.bind.JAXBException;
import jakarta.xml.bind.Unmarshaller;
import java.io.StringReader;

/**
 * Implementación del servicio especializado para procesar documentos con complemento CartaPorte v3.1.
 *
 * Maneja facturas con complemento de traslado (TipoDocumento="T" o "I" con CartaPorte)
 * utilizando el XSD CartaPorte31.xsd del SAT mexicano.
 *
 * @author g_dco018
 * @version 1.0
 * @since 2025
 */
@Service
@Slf4j
public class CartaPorteXmlProcessorServiceImpl implements CartaPorteXmlProcessorService {

    private final MessageCatalogService messageCatalog;
    private JAXBContext jaxbContext;

    public CartaPorteXmlProcessorServiceImpl(MessageCatalogService messageCatalog) {
        this.messageCatalog = messageCatalog;
        try {
            // Contexto que incluye tanto el CFDI como el CartaPorte
            this.jaxbContext = JAXBContext.newInstance(InvoiceXmlDto.class, CartaPorteDto.class);
            log.info("JAXB Context inicializado para CartaPorte v3.1");
        } catch (JAXBException e) {
            log.error("Error inicializando JAXB Context para CartaPorte", e);
            messageCatalog.throwError(FiscalMessageCode.ERR008, e.getMessage(), e);
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public InvoiceXmlDto processCartaPorte(String xmlContent) {
        log.debug("Procesando CFDI con complemento CartaPorte v3.1");

        try {
            Unmarshaller unmarshaller = jaxbContext.createUnmarshaller();
            InvoiceXmlDto invoice = (InvoiceXmlDto) unmarshaller.unmarshal(new StringReader(xmlContent));

            // Validar que efectivamente tenga CartaPorte
            validateCartaPortePresence(invoice);

            // Validaciones específicas de CartaPorte
            validateCartaPorteData(invoice.getCartaPorte());

            log.info("CFDI con CartaPorte procesado exitosamente. Serie: {}, Folio: {}, IdCCP: {}",
                    invoice.getSerie(),
                    invoice.getFolio(),
                    invoice.getCartaPorte() != null ? invoice.getCartaPorte().getIdCCP() : "N/A");

            return invoice;

        } catch (JAXBException e) {
            log.error("Error unmarshalling CartaPorte XML", e);
            messageCatalog.throwError(FiscalMessageCode.ERR011, e.getMessage(), e);
        }
        return null; // Nunca alcanza aquí
    }

    /**
     * Valida que el comprobante tenga el complemento CartaPorte.
     *
     * @param invoice DTO del comprobante
     */
    private void validateCartaPortePresence(InvoiceXmlDto invoice) {
        if (invoice.getCartaPorte() == null) {
            messageCatalog.throwError(FiscalMessageCode.ERR011);
        }

        String tipoComprobante = invoice.getTipoDeComprobante();
        if (!"T".equals(tipoComprobante) && !"I".equals(tipoComprobante)) {
            messageCatalog.throwError(FiscalMessageCode.ERR019,
                String.format("Tipo: %s", tipoComprobante));
        }

        log.debug("Complemento CartaPorte validado correctamente");
    }

    /**
     * Valida los datos específicos del complemento CartaPorte.
     *
     * @param cartaPorte DTO del complemento CartaPorte
     */
    private void validateCartaPorteData(CartaPorteDto cartaPorte) {
        if (cartaPorte == null) {
            messageCatalog.throwError(FiscalMessageCode.ERR011);
        }

        // Validar campos obligatorios
        if (cartaPorte.getVersion() == null || cartaPorte.getVersion().isEmpty()) {
            messageCatalog.throwError(FiscalMessageCode.BUS020);
        }

        if (cartaPorte.getUbicaciones() == null || cartaPorte.getUbicaciones().isEmpty()) {
            messageCatalog.throwError(FiscalMessageCode.ERR011);
        }

        if (cartaPorte.getUbicaciones().size() < 2) {
            messageCatalog.throwError(FiscalMessageCode.ERR011);
        }

        if (cartaPorte.getMercancias() == null) {
            messageCatalog.throwError(FiscalMessageCode.ERR011);
        }

        // Validar mercancías
        validateMercancias(cartaPorte);

        log.debug("Validación de datos CartaPorte completada");
    }

    /**
     * Valida los datos de mercancías en el CartaPorte.
     *
     * @param cartaPorte DTO del CartaPorte
     */
    private void validateMercancias(CartaPorteDto cartaPorte) {
        var mercancias = cartaPorte.getMercancias();

        if (mercancias.getNumTotalMercancias() == null || mercancias.getNumTotalMercancias().isEmpty()) {
            messageCatalog.throwError(FiscalMessageCode.ERR011);
        }

        if (mercancias.getPesoBrutoTotal() == null || mercancias.getPesoBrutoTotal().isEmpty()) {
            messageCatalog.throwError(FiscalMessageCode.ERR011);
        }

        if (mercancias.getMercancias() == null || mercancias.getMercancias().isEmpty()) {
            messageCatalog.throwError(FiscalMessageCode.ERR011);
        }

        log.debug("Validación de mercancías CartaPorte completada");
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String extractTransportInfo(String xmlContent) {
        try {
            InvoiceXmlDto invoice = processCartaPorte(xmlContent);
            CartaPorteDto cartaPorte = invoice.getCartaPorte();

            if (cartaPorte == null) {
                return "Sin información de CartaPorte";
            }

            return String.format("CartaPorte - IdCCP: %s, Mercancías: %s, Peso Total: %s kg",
                    cartaPorte.getIdCCP(),
                    cartaPorte.getMercancias() != null ? cartaPorte.getMercancias().getNumTotalMercancias() : "N/A",
                    cartaPorte.getMercancias() != null ? cartaPorte.getMercancias().getPesoBrutoTotal() : "N/A");

        } catch (Exception e) {
            return "Error extrayendo información de transporte: " + e.getMessage();
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public CartaPorteDto processCartaPorteOnly(String xmlContent) {
        log.debug("Procesando solo complemento CartaPorte");

        try {
            JAXBContext cartaPorteContext = JAXBContext.newInstance(CartaPorteDto.class);
            Unmarshaller unmarshaller = cartaPorteContext.createUnmarshaller();

            CartaPorteDto cartaPorte = (CartaPorteDto) unmarshaller.unmarshal(new StringReader(xmlContent));
            validateCartaPorteData(cartaPorte);

            return cartaPorte;

        } catch (JAXBException e) {
            log.error("Error procesando CartaPorte independiente", e);
            messageCatalog.throwError(FiscalMessageCode.ERR011, e.getMessage(), e);
        }
        return null; // Nunca alcanza aquí
    }
}

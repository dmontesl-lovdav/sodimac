package com.sodimac.fiscal.api.service.impl;

import com.sodimac.fiscal.api.model.dto.invoicexml.DoctoRelacionadoDto;
import com.sodimac.fiscal.api.model.dto.invoicexml.PagosDto;
import com.sodimac.fiscal.api.model.enums.FiscalMessageCode;
import com.sodimac.fiscal.api.service.MessageCatalogService;
import com.sodimac.fiscal.api.service.PagosXmlProcessorService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import jakarta.xml.bind.JAXBContext;
import jakarta.xml.bind.JAXBException;
import jakarta.xml.bind.Unmarshaller;
import java.io.StringReader;
import java.util.List;

/**
 * Implementación del servicio especializado para procesar complementos de Pagos v2.0.
 *
 * Maneja complementos de pago (TipoDocumento="P") utilizando el XSD
 * Pagos20.xsd del SAT mexicano.
 *
 * @author g_dco018
 * @version 1.0
 * @since 2025
 */
@Service
@Slf4j
public class PagosXmlProcessorServiceImpl implements PagosXmlProcessorService {

    private JAXBContext jaxbContext;
    private final MessageCatalogService messageCatalog;

    public PagosXmlProcessorServiceImpl(MessageCatalogService messageCatalog) {
        this.messageCatalog = messageCatalog;
        try {
            this.jaxbContext = JAXBContext.newInstance(PagosDto.class);
            log.info("JAXB Context inicializado para Pagos v2.0");
        } catch (JAXBException e) {
            log.error("Error inicializando JAXB Context para Pagos", e);
            messageCatalog.throwError(FiscalMessageCode.ERR010, e.getMessage(), e);
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public PagosDto processPagos(String xmlContent) {
        log.debug("Procesando complemento Pagos v2.0");

        try {
            Unmarshaller unmarshaller = jaxbContext.createUnmarshaller();
            PagosDto pagos = (PagosDto) unmarshaller.unmarshal(new StringReader(xmlContent));

            // Validaciones específicas de Pagos
            validatePagosData(pagos);

            log.info("Complemento de Pagos procesado exitosamente. Total pagos: {}, Monto total: {}",
                    pagos.getPagos() != null ? pagos.getPagos().size() : 0,
                    pagos.getTotales() != null ? pagos.getTotales().getMontoTotalPagos() : "N/A");

            return pagos;

        } catch (JAXBException e) {
            log.error("Error unmarshalling Pagos XML", e);
            messageCatalog.throwError(FiscalMessageCode.ERR013, e.getMessage(), e);
        }
        return null; // Nunca alcanza aquí por el throw anterior
    }

    /**
     * Valida los datos del complemento Pagos.
     *
     * @param pagos DTO del complemento Pagos
     */
    private void validatePagosData(PagosDto pagos) {
        if (pagos == null) {
            messageCatalog.throwError(FiscalMessageCode.ERR021);
        }

        // Validar totales
        if (pagos.getTotales() == null) {
            messageCatalog.throwError(FiscalMessageCode.ERR022);
        }

        // Validar que tenga al menos un pago
        if (pagos.getPagos() == null || pagos.getPagos().isEmpty()) {
            messageCatalog.throwError(FiscalMessageCode.ERR024);
        }

        // Validar cada pago individual
        validateIndividualPayments(pagos);

        log.debug("Validación de complemento Pagos completada");
    }

    /**
     * Valida cada pago individual dentro del complemento.
     *
     * @param pagos DTO del complemento Pagos
     */
    private void validateIndividualPayments(PagosDto pagos) {
        for (int i = 0; i < pagos.getPagos().size(); i++) {
            var pago = pagos.getPagos().get(i);

            if (pago.getFechaPago() == null || pago.getFechaPago().isEmpty()) {
                messageCatalog.throwError(FiscalMessageCode.ERR013,
                    String.format("Pago %d - Fecha faltante", i + 1));
            }

            if (pago.getFormaDePagoP() == null || pago.getFormaDePagoP().isEmpty()) {
                messageCatalog.throwError(FiscalMessageCode.ERR013,
                    String.format("Pago %d - Forma de pago faltante", i + 1));
            }

            if (pago.getMonto() == null || pago.getMonto().isEmpty()) {
                messageCatalog.throwError(FiscalMessageCode.ERR013,
                    String.format("Pago %d - Monto faltante", i + 1));
            }

            // Validar documentos relacionados
            if (pago.getDoctosRelacionados() == null || pago.getDoctosRelacionados().isEmpty()) {
                messageCatalog.throwError(FiscalMessageCode.ERR031,
                    String.format("Pago %d - Sin documentos relacionados", i + 1));
            }

            validateRelatedDocuments(pago.getDoctosRelacionados(), i + 1);
        }

        log.debug("Validación de pagos individuales completada");
    }

    /**
     * Valida los documentos relacionados de un pago.
     *
     * @param documentos Lista de documentos relacionados
     * @param numeroPago Número del pago para mensajes de error
     */
    private void validateRelatedDocuments(List<DoctoRelacionadoDto> documentos, int numeroPago) {
        for (int i = 0; i < documentos.size(); i++) {
            var documento = documentos.get(i);

            if (documento.getIdDocumento() == null || documento.getIdDocumento().isEmpty()) {
                messageCatalog.throwError(FiscalMessageCode.ERR031,
                        String.format("Documento %d del pago %d - ID faltante", i + 1, numeroPago));
            }

            if (documento.getImpPagado() == null || documento.getImpPagado().isEmpty()) {
                messageCatalog.throwError(FiscalMessageCode.ERR013,
                        String.format("Documento %d del pago %d - Importe pagado faltante", i + 1, numeroPago));
            }

            if (documento.getImpSaldoInsoluto() == null || documento.getImpSaldoInsoluto().isEmpty()) {
                messageCatalog.throwError(FiscalMessageCode.ERR013,
                        String.format("Documento %d del pago %d - Saldo insoluto faltante", i + 1, numeroPago));
            }
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String extractPaymentInfo(String xmlContent) {
        try {
            PagosDto pagos = processPagos(xmlContent);

            int totalPagos = pagos.getPagos() != null ? pagos.getPagos().size() : 0;
            String montoTotal = pagos.getTotales() != null ? pagos.getTotales().getMontoTotalPagos() : "0";

            StringBuilder info = new StringBuilder();
            info.append(String.format("Complemento Pagos - Total pagos: %d, Monto total: %s MXN", totalPagos, montoTotal));

            if (pagos.getPagos() != null && !pagos.getPagos().isEmpty()) {
                var primerPago = pagos.getPagos().get(0);
                info.append(String.format(", Primera fecha: %s", primerPago.getFechaPago()));
            }

            return info.toString();

        } catch (Exception e) {
            return "Error extrayendo información de pagos: " + e.getMessage();
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String calculateTotalAmount(String xmlContent) {
        try {
            PagosDto pagos = processPagos(xmlContent);

            if (pagos.getTotales() != null && pagos.getTotales().getMontoTotalPagos() != null) {
                return pagos.getTotales().getMontoTotalPagos();
            }

            // Si no hay total en totales, sumar manualmente
            double total = 0.0;
            if (pagos.getPagos() != null) {
                for (var pago : pagos.getPagos()) {
                    if (pago.getMonto() != null) {
                        try {
                            total += Double.parseDouble(pago.getMonto());
                        } catch (NumberFormatException e) {
                            log.warn("No se puede parsear monto: {}", pago.getMonto());
                        }
                    }
                }
            }

            return String.valueOf(total);

        } catch (Exception e) {
            log.error("Error calculando monto total de pagos", e);
            return "0";
        }
    }
}

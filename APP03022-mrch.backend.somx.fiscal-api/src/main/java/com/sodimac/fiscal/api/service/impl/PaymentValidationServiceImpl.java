package com.sodimac.fiscal.api.service.impl;

import com.sodimac.fiscal.api.model.dto.ParsedPaymentXmlDto;
import com.sodimac.fiscal.api.model.dto.invoicexml.DoctoRelacionadoDto;
import com.sodimac.fiscal.api.model.dto.invoicexml.PagoDto;
import com.sodimac.fiscal.api.model.entity.InvoiceEntity;
import com.sodimac.fiscal.api.model.entity.PaymentsEntity;
import com.sodimac.fiscal.api.model.entity.VersionCatalogEntity;
import com.sodimac.fiscal.api.model.enums.FiscalMessageCode;
import com.sodimac.fiscal.api.repository.AddendumRepository;
import com.sodimac.fiscal.api.repository.InvoiceRepository;
import com.sodimac.fiscal.api.repository.PaymentsRepository;
import com.sodimac.fiscal.api.repository.VersionCatalogRepository;
import com.sodimac.fiscal.api.service.MessageCatalogService;
import com.sodimac.fiscal.api.service.PaymentValidationService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

/**
 * Implementación del servicio de validaciones de complementos de pago.
 *
 * @author Sodimac Tech Team
 * @version 1.0
 * @since 2025
 */
@Service
@Slf4j
@RequiredArgsConstructor
public class PaymentValidationServiceImpl implements PaymentValidationService {

    private final MessageCatalogService messageCatalog;
    private final AddendumRepository addendumRepository;
    private final VersionCatalogRepository versionCatalogRepository;
    private final PaymentsRepository paymentsRepository;
    private final InvoiceRepository invoiceRepository;

    @Override
    public void validateAuthorizedReceiver(String rfcReceptor) {
        log.debug("Validando receptor autorizado: {}", rfcReceptor);

        // Catálogo CatRfcReceptor (shared_catalogs). Reemplaza la tabla authorized_receiver_catalog
        // (decisión Ivan 2026-06-23).
        if (!addendumRepository.existsRfcReceptorAutorizado(rfcReceptor)) {
            log.error("RFC Receptor '{}' no está autorizado en el sistema", rfcReceptor);
            messageCatalog.throwError(FiscalMessageCode.ERR029,
                String.format("RFC: %s", rfcReceptor));
        }

        log.debug("Receptor autorizado validado exitosamente");
    }

    @Override
    public void validatePaymentVersion(String version, String documentType) {
        log.debug("Validando versión de complemento de pago: {} para documento tipo: {}", version, documentType);

        // Convertir version string a BigDecimal
        BigDecimal versionDecimal;
        try {
            versionDecimal = new BigDecimal(version);
        } catch (NumberFormatException e) {
            messageCatalog.throwError(FiscalMessageCode.ERR025,
                String.format("Formato inválido: %s", version));
            return; // Nunca alcanza aquí
        }

        // Buscar versión vigente en catálogo
        Optional<VersionCatalogEntity> versionEntity =
                versionCatalogRepository.findByVersionAndDocumentTypeAndStatus(
                        versionDecimal,
                        documentType,
                        1
                );

        if (versionEntity.isEmpty()) {
            log.error("La versión {} del complemento de pago no es vigente en el sistema", version);
            messageCatalog.throwError(FiscalMessageCode.ERR030,
                String.format("Versión: %s, Tipo: %s", version, documentType));
        }

        log.debug("Versión de complemento de pago validada exitosamente");
    }

    @Override
    public void validateNoDuplicate(UUID fiscalUuid) {
        log.debug("Validando que el complemento de pago no esté duplicado: {}", fiscalUuid);

        Optional<PaymentsEntity> existing = paymentsRepository.findByFiscalUuid(fiscalUuid);

        if (existing.isPresent()) {
            log.error("El complemento de pago con UUID {} ya se encuentra registrado en el sistema", fiscalUuid);
            messageCatalog.throwError(FiscalMessageCode.ERR026,
                String.format("UUID: %s", fiscalUuid));
        }

        log.debug("Complemento de pago no duplicado, validación exitosa");
    }

    @Override
    public void validateAddendaType(Integer tipoAddenda) {
        log.debug("Validando tipo de addenda: {}", tipoAddenda);

        if (tipoAddenda == null || tipoAddenda != 5) {
            log.error("Tipo de addenda inválido: {}. Para complementos de pago debe ser 5", tipoAddenda);
            messageCatalog.throwError(FiscalMessageCode.ERR028,
                String.format("Recibido: %s, Esperado: 5", tipoAddenda));
        }

        log.debug("Tipo de addenda validado exitosamente");
    }

    @Override
    public Map<UUID, UUID> validateAndResolveRelatedDocuments(ParsedPaymentXmlDto parsedXml) {
        log.debug("Validando existencia de documentos relacionados (DoctoRelacionado.IdDocumento)");

        Map<UUID, UUID> fiscalToInvoiceUuid = new HashMap<>();

        if (parsedXml.getPagos() == null || parsedXml.getPagos().getPagos() == null) {
            log.warn("El complemento no trae nodos Pago; no hay documentos relacionados que validar");
            return fiscalToInvoiceUuid;
        }

        for (PagoDto pago : parsedXml.getPagos().getPagos()) {
            if (pago.getDoctosRelacionados() == null) {
                continue;
            }
            for (DoctoRelacionadoDto docto : pago.getDoctosRelacionados()) {
                String idDocumento = docto.getIdDocumento();
                if (idDocumento == null || idDocumento.isBlank()) {
                    log.error("DoctoRelacionado sin IdDocumento (Serie={}, Folio={})",
                            docto.getSerie(), docto.getFolio());
                    messageCatalog.throwError(FiscalMessageCode.ERR031,
                            String.format("Serie: %s, Folio: %s", docto.getSerie(), docto.getFolio()));
                }

                UUID fiscalUuid;
                try {
                    fiscalUuid = UUID.fromString(idDocumento.trim());
                } catch (IllegalArgumentException ex) {
                    log.error("IdDocumento no es un UUID válido: {}", idDocumento);
                    messageCatalog.throwError(FiscalMessageCode.ERR031,
                            String.format("IdDocumento inválido: %s", idDocumento));
                    return fiscalToInvoiceUuid;
                }

                if (fiscalToInvoiceUuid.containsKey(fiscalUuid)) {
                    continue;
                }

                // IdDocumento del SAT = fiscal_uuid; el FK related_documents.document_uuid
                // apunta a invoice.invoice_uuid (PK interno).
                Optional<InvoiceEntity> invoice = invoiceRepository.findByFiscalUuid(fiscalUuid);
                if (invoice.isEmpty()) {
                    log.error(
                            "Documento relacionado no registrado en invoice. fiscal_uuid={}, serie={}, folio={}",
                            fiscalUuid, docto.getSerie(), docto.getFolio());
                    messageCatalog.throwError(FiscalMessageCode.ERR031,
                            String.format("UUID fiscal: %s, Serie: %s, Folio: %s",
                                    fiscalUuid, docto.getSerie(), docto.getFolio()));
                }

                fiscalToInvoiceUuid.put(fiscalUuid, invoice.get().getInvoiceUuid());
                log.debug("Documento relacionado resuelto: fiscalUuid={} → invoiceUuid={}",
                        fiscalUuid, invoice.get().getInvoiceUuid());
            }
        }

        log.debug("Documentos relacionados validados: {}", fiscalToInvoiceUuid.size());
        return fiscalToInvoiceUuid;
    }

    @Override
    public void validateComprobanteType(String tipoComprobante) {
        log.debug("Validando tipo de comprobante: {}", tipoComprobante);

        if (!"P".equals(tipoComprobante)) {
            log.error("Tipo de comprobante inválido: {}. Debe ser 'P' para complementos de pago", tipoComprobante);
            messageCatalog.throwError(FiscalMessageCode.ERR020,
                String.format("Recibido: %s, Esperado: P", tipoComprobante));
        }

        log.debug("Tipo de comprobante validado exitosamente");
    }
}

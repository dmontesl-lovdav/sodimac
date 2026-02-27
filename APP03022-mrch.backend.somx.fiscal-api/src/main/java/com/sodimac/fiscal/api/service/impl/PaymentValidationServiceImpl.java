package com.sodimac.fiscal.api.service.impl;

import com.sodimac.fiscal.api.model.dto.ParsedPaymentXmlDto;
import com.sodimac.fiscal.api.model.entity.AuthorizedReceiverCatalogEntity;
import com.sodimac.fiscal.api.model.entity.PaymentsEntity;
import com.sodimac.fiscal.api.model.entity.VersionCatalogEntity;
import com.sodimac.fiscal.api.model.enums.FiscalMessageCode;
import com.sodimac.fiscal.api.repository.AuthorizedReceiverCatalogRepository;
import com.sodimac.fiscal.api.repository.PaymentsRepository;
import com.sodimac.fiscal.api.repository.VersionCatalogRepository;
import com.sodimac.fiscal.api.service.MessageCatalogService;
import com.sodimac.fiscal.api.service.PaymentValidationService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
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
    private final AuthorizedReceiverCatalogRepository authorizedReceiverRepository;
    private final VersionCatalogRepository versionCatalogRepository;
    private final PaymentsRepository paymentsRepository;

    @Override
    public void validateAuthorizedReceiver(String rfcReceptor) {
        log.debug("Validando receptor autorizado: {}", rfcReceptor);

        Optional<AuthorizedReceiverCatalogEntity> authorized =
                authorizedReceiverRepository.findByRfcAndStatus(rfcReceptor, 1);

        if (authorized.isEmpty()) {
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
    public void validateRelatedDocumentsExist(ParsedPaymentXmlDto parsedXml) {
        log.debug("Validando existencia de documentos relacionados");

        // TODO: Implementar validación de documentos relacionados
        // Requiere parsear el nodo Pagos completo con PagosXmlProcessorService
        // y luego verificar cada DoctoRelacionado.IdDocumento contra la tabla invoice

        log.debug("Validación de documentos relacionados pendiente de implementación completa");
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

package com.sodimac.fiscal.api.service;

import com.sodimac.fiscal.api.model.dto.ParsedPaymentXmlDto;

import java.util.Map;
import java.util.UUID;

/**
 * Servicio para validaciones de negocio de complementos de pago.
 *
 * @author Sodimac Tech Team
 * @version 1.0
 * @since 2025
 */
public interface PaymentValidationService {

    /**
     * Valida que el RFC del receptor esté autorizado en el sistema.
     *
     * @param rfcReceptor RFC del receptor
     * @throws RuntimeException si el receptor no está autorizado
     */
    void validateAuthorizedReceiver(String rfcReceptor);

    /**
     * Valida que la versión del complemento de pago sea vigente.
     *
     * @param version Versión del complemento (ej: 2.0)
     * @param documentType Tipo de documento ('P' para pagos)
     * @throws RuntimeException si la versión no es vigente
     */
    void validatePaymentVersion(String version, String documentType);

    /**
     * Valida que el complemento de pago no esté duplicado.
     *
     * @param uuid UUID del complemento de pago
     * @throws RuntimeException si ya existe
     */
    void validateNoDuplicate(UUID uuid);

    /**
     * Valida que el tipo de addenda sea correcto para complementos de pago.
     *
     * @param tipoAddenda Tipo de addenda (debe ser 5)
     * @throws RuntimeException si el tipo no es válido
     */
    void validateAddendaType(Integer tipoAddenda);

    /**
     * Valida que todos los DoctoRelacionado (IdDocumento = UUID fiscal SAT)
     * existan en {@code invoice} y resuelve el PK interno ({@code invoice_uuid})
     * requerido por el FK {@code related_documents.document_uuid}.
     *
     * @param parsedXml Datos parseados del XML
     * @return mapa fiscalUuid → invoiceUuid
     * @throws RuntimeException si algún documento relacionado no existe (ERR031)
     */
    Map<UUID, UUID> validateAndResolveRelatedDocuments(ParsedPaymentXmlDto parsedXml);

    /**
     * Valida el tipo de comprobante (debe ser 'P').
     *
     * @param tipoComprobante Tipo de comprobante
     * @throws RuntimeException si no es tipo 'P'
     */
    void validateComprobanteType(String tipoComprobante);
}

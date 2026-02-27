package com.sodimac.fiscal.api.model.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.UUID;

/**
 * DTO de respuesta para el registro de complementos de pago.
 *
 * @author Sodimac Tech Team
 * @version 1.0
 * @since 2025
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PaymentRegistrationResponse {

    /**
     * UUID del complemento de pago registrado.
     */
    private UUID paymentsUuid;

    /**
     * Nombre del archivo procesado.
     */
    private String fileName;

    /**
     * Estado del procesamiento.
     */
    private String processingStatus;

    /**
     * Código de respuesta.
     */
    private String responseCode;

    /**
     * Mensaje de respuesta.
     */
    private String message;

    /**
     * Folio del complemento de pago.
     */
    private String folio;

    /**
     * Serie del complemento de pago.
     */
    private String serie;

    /**
     * RFC del emisor.
     */
    private String rfcEmisor;

    /**
     * RFC del receptor.
     */
    private String rfcReceptor;

    /**
     * Monto total de los pagos.
     */
    private String montoTotalPagos;

    /**
     * Fecha de registro.
     */
    private LocalDateTime fechaRegistro;

    /**
     * UUID del log generado.
     */
    private UUID logUuid;
}

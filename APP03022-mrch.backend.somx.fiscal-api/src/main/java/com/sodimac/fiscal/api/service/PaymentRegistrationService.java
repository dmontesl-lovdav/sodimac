package com.sodimac.fiscal.api.service;

import com.sodimac.fiscal.api.model.dto.request.PaymentRegistrationRequest;
import com.sodimac.fiscal.api.model.dto.response.PaymentRegistrationResponse;

/**
 * Servicio principal para el registro de complementos de pago.
 *
 * Orquesta todo el proceso de validación, parseo y registro de complementos de pago
 * según las reglas de negocio establecidas.
 *
 * @author Sodimac Tech Team
 * @version 1.0
 * @since 2025
 */
public interface PaymentRegistrationService {

    /**
     * Registra un complemento de pago en el sistema.
     *
     * Proceso completo:
     * 1. Validar estructura XML contra XSD
     * 2. Validar con SAT vía multipac (Detecno)
     * 3. Parsear XML
     * 4. Validaciones de negocio
     * 5. Registrar en base de datos (transaccional)
     * 6. Guardar log y registro de archivo
     *
     * @param request DTO con los parámetros de entrada
     * @return DTO con la respuesta del registro
     */
    PaymentRegistrationResponse registerPayment(PaymentRegistrationRequest request);
}

package com.sodimac.fiscal.api.service;

import com.sodimac.fiscal.api.model.dto.StatusTrainValidationResult;

/**
 * Servicio cliente para consumir el API de Tren de Estatus (STM-1166).
 *
 * Valida las transiciones de estatus contra el servicio centralizado
 * en catalogos-api.
 *
 * @author Sodimac Tech Team
 * @since STM-410
 */
public interface StatusTrainApiService {

    /**
     * Valida si una transición de estatus está permitida.
     *
     * @param optionId ID de la opción (1=Factura, 2=NC, 3=Pagos, 4=CartaPorte)
     * @param sourceStatus Estatus origen actual
     * @param targetStatus Estatus destino deseado
     * @return Resultado de la validación con código de error si aplica
     */
    StatusTrainValidationResult validateTransition(Integer optionId, Integer sourceStatus, Integer targetStatus);

    /**
     * Verifica si el servicio de status-train está habilitado.
     *
     * @return true si el servicio está habilitado y disponible
     */
    boolean isEnabled();
}

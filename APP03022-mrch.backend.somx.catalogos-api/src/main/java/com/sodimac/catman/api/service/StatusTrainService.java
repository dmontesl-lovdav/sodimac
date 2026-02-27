package com.sodimac.catman.api.service;

import java.util.Optional;

import com.sodimac.catman.api.model.dto.StatusTrainCreateDto;
import com.sodimac.catman.api.model.dto.StatusTrainDto;
import com.sodimac.catman.api.model.dto.StatusTrainListResponse;
import com.sodimac.catman.api.model.dto.StatusTrainUpdateDto;
import com.sodimac.catman.api.model.dto.StatusTrainValidationResponse;

/**
 * Servicio para gestión del tren de estatus.
 * Proporciona operaciones para validar y administrar transiciones de estatus.
 *
 * @author Sodimac Tech Team
 * @since STM-1166
 */
public interface StatusTrainService {

    /**
     * Valida si una transición de estatus está permitida.
     *
     * @param optionId ID de la opción (1=Factura, 2=NC, 3=Pagos, 4=CartaPorte)
     * @param sourceStatus Estatus origen
     * @param targetStatus Estatus destino
     * @return Respuesta con el resultado de la validación
     */
    StatusTrainValidationResponse validateTransition(Integer optionId, Integer sourceStatus, Integer targetStatus);

    /**
     * Obtiene todos los estatus destino permitidos desde un estatus origen.
     *
     * @param optionId ID de la opción
     * @param sourceStatus Estatus origen
     * @return Respuesta con la lista de destinos permitidos
     */
    StatusTrainListResponse getAllowedDestinations(Integer optionId, Integer sourceStatus);

    /**
     * Crea una nueva regla de transición.
     *
     * @param createDto Datos de la nueva regla
     * @return DTO de la regla creada
     */
    StatusTrainDto create(StatusTrainCreateDto createDto);

    /**
     * Actualiza una regla de transición existente.
     *
     * @param id ID de la regla a actualizar
     * @param optionId ID de la opción (para validación)
     * @param updateDto Datos a actualizar
     * @return Optional con la regla actualizada, vacío si no existe
     */
    Optional<StatusTrainDto> update(Integer id, Integer optionId, StatusTrainUpdateDto updateDto);

    /**
     * Elimina una regla de transición.
     *
     * @param id ID de la regla a eliminar
     * @return true si se eliminó, false si no existía
     */
    boolean delete(Integer id);

    /**
     * Obtiene una regla por su ID.
     *
     * @param id ID de la regla
     * @return Optional con la regla, vacío si no existe
     */
    Optional<StatusTrainDto> findById(Integer id);
}

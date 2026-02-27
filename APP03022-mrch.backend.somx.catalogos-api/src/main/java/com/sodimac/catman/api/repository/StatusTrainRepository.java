package com.sodimac.catman.api.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.sodimac.catman.api.model.entity.StatusTrain;

/**
 * Repositorio para la entidad StatusTrain.
 * Proporciona métodos de consulta para validar transiciones de estatus.
 *
 * @author Sodimac Tech Team
 * @since STM-1166
 */
@Repository
public interface StatusTrainRepository extends JpaRepository<StatusTrain, Integer> {

    /**
     * Busca una transición específica por opción, estatus origen y destino.
     * Usado para validar si una transición está permitida.
     *
     * @param optionId ID de la opción (1=Factura, 2=NC, 3=Pagos, 4=CartaPorte)
     * @param sourceStatus Estatus origen
     * @param targetStatus Estatus destino
     * @return Optional con la transición si existe
     */
    Optional<StatusTrain> findByOptionIdAndSourceStatusAndTargetStatus(
            Integer optionId, Integer sourceStatus, Integer targetStatus);

    /**
     * Obtiene todos los destinos permitidos desde un estatus origen para una opción.
     *
     * @param optionId ID de la opción
     * @param sourceStatus Estatus origen
     * @return Lista de transiciones permitidas
     */
    List<StatusTrain> findByOptionIdAndSourceStatus(Integer optionId, Integer sourceStatus);

    /**
     * Obtiene todas las transiciones configuradas para una opción.
     *
     * @param optionId ID de la opción
     * @return Lista de todas las transiciones de la opción
     */
    List<StatusTrain> findByOptionId(Integer optionId);

    /**
     * Verifica si existe al menos una transición desde un estatus origen para una opción.
     * Usado para validar que el estatus origen está catalogado.
     *
     * @param optionId ID de la opción
     * @param sourceStatus Estatus origen
     * @return true si existe al menos una transición
     */
    boolean existsByOptionIdAndSourceStatus(Integer optionId, Integer sourceStatus);

    /**
     * Verifica si existe una transición específica.
     *
     * @param optionId ID de la opción
     * @param sourceStatus Estatus origen
     * @param targetStatus Estatus destino
     * @return true si la transición existe
     */
    boolean existsByOptionIdAndSourceStatusAndTargetStatus(
            Integer optionId, Integer sourceStatus, Integer targetStatus);
}

package com.sodimac.fiscal.api.repository;

import com.sodimac.fiscal.api.model.entity.StatusTrainEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

/**
 * Lectura DIRECTA de shared_catalogs.status_train (misma BD), sin util-api.
 * Valida si una transición (option_id, origen -> destino) está permitida en el tren.
 */
@Repository
public interface StatusTrainRepository extends JpaRepository<StatusTrainEntity, Integer> {

    boolean existsByOptionIdAndSourceStatusAndTargetStatus(Integer optionId, Integer sourceStatus, Integer targetStatus);
}

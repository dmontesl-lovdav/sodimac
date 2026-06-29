package com.sodimac.fiscal.api.repository;

import com.sodimac.fiscal.api.model.entity.ReceptionEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface ReceptionRepository extends JpaRepository<ReceptionEntity, UUID> {

    /**
     * Busca la recepción por su número (reception_number). La factura no guarda el UUID de la
     * recepción, solo el número en addendum.reception_number; este método permite resolver la
     * recepción (monto + estatus) a partir de ese número para el recálculo de tolerancia con NCs
     * (fila 104 QA).
     */
    Optional<ReceptionEntity> findByReceptionNumber(String receptionNumber);
}

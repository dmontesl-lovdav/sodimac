package com.sodimac.batch.process.repository.sap;

import com.sodimac.batch.process.model.entity.sap.ComprobanteEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface ComprobanteRepository extends JpaRepository<ComprobanteEntity, Integer> {
    Optional<ComprobanteEntity> findByFiscalUuid(String fiscalUuid);
    boolean existsByFiscalUuid(String fiscalUuid);
}

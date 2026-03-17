package com.sodimac.batch.fiscal.download.repository.sap;

import com.sodimac.batch.fiscal.download.model.entity.sap.ComprobanteEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface ComprobanteRepository extends JpaRepository<ComprobanteEntity, Integer> {
    Optional<ComprobanteEntity> findByFiscalUuid(String fiscalUuid);
    boolean existsByFiscalUuid(String fiscalUuid);
}

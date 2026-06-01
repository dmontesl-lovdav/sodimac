package com.rebatesync.infrastructure.adapter.persistence.repository;

import com.rebatesync.infrastructure.adapter.persistence.entity.RebateAgreementEntity;
import com.rebatesync.infrastructure.adapter.persistence.entity.RebateAgreementEntityId;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * Repositorio JPA para RebateAgreementEntity
 *
 * Usa RebateAgreementEntityId como clave compuesta (supplierNumber + agreementNumber)
 */
@Repository
public interface JpaRebateAgreementRepository extends JpaRepository<RebateAgreementEntity, RebateAgreementEntityId> {

    List<RebateAgreementEntity> findBySupplierNumber(String supplierNumber);

    /**
     * Elimina todos los registros sin leer primero (usa query nativa con schema completo)
     */
    @Modifying
    @Query(value = "DELETE FROM SODIMAC_REBATES_DEV.dbo.RebateAcuerdosTemp", nativeQuery = true)
    void deleteAllInBatch();
}

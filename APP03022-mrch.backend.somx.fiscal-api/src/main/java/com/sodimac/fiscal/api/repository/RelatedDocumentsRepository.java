package com.sodimac.fiscal.api.repository;

import com.sodimac.fiscal.api.model.entity.RelatedDocumentsEntity;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.UUID;

@Repository
public interface RelatedDocumentsRepository extends JpaRepository<RelatedDocumentsEntity, UUID> {

    /**
     * Busca documentos relacionados por UUID del complemento de pago (payments).
     *
     * @param paymentsUuid UUID del complemento de pago
     * @param pageable Configuración de paginación
     * @return Página de documentos relacionados
     */
    @Query("SELECT rd FROM RelatedDocumentsEntity rd " +
           "WHERE rd.paymentUuid IN (SELECT p.paymentUuid FROM PaymentEntity p WHERE p.paymentsUuid = :paymentsUuid)")
    Page<RelatedDocumentsEntity> findByPaymentsUuid(@Param("paymentsUuid") UUID paymentsUuid, Pageable pageable);

}
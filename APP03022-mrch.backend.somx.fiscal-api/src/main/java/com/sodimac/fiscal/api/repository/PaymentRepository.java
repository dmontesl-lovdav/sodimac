package com.sodimac.fiscal.api.repository;

import com.sodimac.fiscal.api.model.entity.PaymentEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface PaymentRepository extends JpaRepository<PaymentEntity, UUID> {

    /**
     * Suma el monto total de todos los pagos de un complemento de pago.
     *
     * @param paymentsUuid UUID del complemento de pago
     * @return Suma total de los montos
     */
    @Query("SELECT COALESCE(SUM(p.amount), 0) FROM PaymentEntity p WHERE p.paymentsUuid = :paymentsUuid")
    Optional<BigDecimal> sumAmountByPaymentsUuid(@Param("paymentsUuid") UUID paymentsUuid);

    /**
     * Cuenta la cantidad de documentos relacionados de un complemento de pago.
     *
     * @param paymentsUuid UUID del complemento de pago
     * @return Cantidad de documentos relacionados
     */
    @Query("SELECT COUNT(rd) FROM RelatedDocumentsEntity rd " +
           "WHERE rd.paymentUuid IN (SELECT p.paymentUuid FROM PaymentEntity p WHERE p.paymentsUuid = :paymentsUuid)")
    Integer countRelatedDocumentsByPaymentsUuid(@Param("paymentsUuid") UUID paymentsUuid);
}
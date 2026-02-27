package com.sodimac.fiscal.api.repository;

import com.sodimac.fiscal.api.model.entity.PaymentsEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface PaymentsRepository extends JpaRepository<PaymentsEntity, UUID>, PaymentsRepositoryCustom {

    /**
     * Busca un complemento de pago por su UUID fiscal (TimbreFiscalDigital del SAT).
     *
     * @param fiscalUuid UUID del TimbreFiscalDigital
     * @return Optional con el complemento si existe
     */
    Optional<PaymentsEntity> findByFiscalUuid(UUID fiscalUuid);

}
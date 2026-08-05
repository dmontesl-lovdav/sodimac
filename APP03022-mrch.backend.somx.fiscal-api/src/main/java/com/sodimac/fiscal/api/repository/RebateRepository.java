package com.sodimac.fiscal.api.repository;

import com.sodimac.fiscal.api.model.entity.RebateEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.UUID;

/**
 * Lectura de tenant_finance.rebate (descuento comercial) por su PK rebate_uuid.
 * Mismo patrón cross-schema que ReceptionRepository (JPA directo, sin HTTP).
 */
@Repository
public interface RebateRepository extends JpaRepository<RebateEntity, UUID> {
}

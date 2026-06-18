package com.sodimac.fiscal.api.repository;

import com.sodimac.fiscal.api.model.entity.AddendumEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface AddendumRepository extends JpaRepository<AddendumEntity, UUID> {

    /**
     * Busca una addenda por UUID de la factura/NC asociada.
     *
     * @param invoiceUuid UUID de la factura/NC
     * @return Optional con la addenda si existe
     */
    Optional<AddendumEntity> findByInvoiceUuid(UUID invoiceUuid);

    /**
     * UUIDs de facturas/NC cuyas addendas referencian alguna de las recepciones dadas
     * (addendum.reception_number guarda el reception_id en texto). Ver fix Fer 2026-06-18.
     */
    @Query("SELECT a.invoiceUuid FROM AddendumEntity a WHERE a.receptionNumber IN :receptionNumbers")
    List<UUID> findInvoiceUuidsByReceptionNumbers(@Param("receptionNumbers") List<String> receptionNumbers);
}
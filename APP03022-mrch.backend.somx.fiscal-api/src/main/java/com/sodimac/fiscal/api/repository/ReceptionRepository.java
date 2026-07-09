package com.sodimac.fiscal.api.repository;

import com.sodimac.fiscal.api.model.entity.ReceptionEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface ReceptionRepository extends JpaRepository<ReceptionEntity, UUID> {

    /**
     * Resuelve la recepción por su número Y la orden de compra (order_number del addendum).
     *
     * reception_number NO es único: el mismo número existe para OCs distintas. La factura guarda
     * en su addenda tanto reception_number como purchase_order_number; la recepción correcta es la
     * que cuadra con AMBOS. Un findBy solo por número devuelve varias filas (NonUniqueResultException
     * -> ERR003 al publicar NC) y ademas puede resolver a una recepción de otra OC. Se une contra
     * purchase_order por order_number para desambiguar. QA filas 104/122.
     *
     * Devuelve lista (defensivo, ordenada por fecha desc) aunque (número + OC) es único.
     */
    @Query(value =
            "SELECT r.* FROM tenant_finance.reception r "
            + "JOIN tenant_finance.purchase_order po ON po.purchase_order_uuid = r.purchase_order_uuid "
            + "WHERE r.reception_number = :receptionNumber AND po.order_number = :orderNumber "
            + "ORDER BY r.created_at DESC",
            nativeQuery = true)
    List<ReceptionEntity> findByReceptionNumberAndOrderNumber(@Param("receptionNumber") String receptionNumber,
                                                              @Param("orderNumber") String orderNumber);

    /**
     * Fallback por número solamente (cuando el addendum no trae purchase_order_number). Devuelve
     * lista ordenada por fecha desc para NO lanzar NonUniqueResultException si el número está
     * repetido; el llamador toma la primera.
     */
    @Query(value =
            "SELECT r.* FROM tenant_finance.reception r "
            + "WHERE r.reception_number = :receptionNumber "
            + "ORDER BY r.created_at DESC",
            nativeQuery = true)
    List<ReceptionEntity> findByReceptionNumberOrdered(@Param("receptionNumber") String receptionNumber);
}

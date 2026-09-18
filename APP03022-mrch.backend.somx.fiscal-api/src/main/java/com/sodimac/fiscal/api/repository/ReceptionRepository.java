package com.sodimac.fiscal.api.repository;

import com.sodimac.fiscal.api.model.entity.ReceptionEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
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

    /**
     * Al consumir una recepción de transporte, las guías ligadas por {@code guide_number} pasan a
     * estatus 3 (CatEstatusCartaPorteFBC: relacionada con OC y factura / Por Contabilizar).
     * Solo desde estatus 2 (con OC / Pendiente de Facturar) para no pisar 4+.
     *
     * @return filas actualizadas (0 si no hay guía o ya no está en 2)
     */
    @Modifying(clearAutomatically = true)
    @Query(value =
            "UPDATE tenant_finance.shipping_guide "
            + "SET status = 3, is_status_updated = true, updated_at = CURRENT_TIMESTAMP "
            + "WHERE TRIM(guide_number) = TRIM(:guideNumber) AND status = 2",
            nativeQuery = true)
    int markShippingGuidesPorContabilizar(@Param("guideNumber") String guideNumber);

    /**
     * Al CANCELAR una factura de transporte, las guías ligadas por {@code guide_number} regresan a
     * estatus 2 (Pendiente de Facturar) para poder re-facturarse. Solo desde estatus 3 (Por
     * Contabilizar, el que dejó el consumo) para no pisar guías ya contabilizadas (4+) ni canceladas.
     * Tabla de conversión de estatus (Ivan 2026-09-03): Factura 20 (Cancelada) -> Carta Porte 2.
     *
     * @return filas actualizadas (0 si no hay guía o no está en 3)
     */
    @Modifying(clearAutomatically = true)
    @Query(value =
            "UPDATE tenant_finance.shipping_guide "
            + "SET status = 2, is_status_updated = true, updated_at = CURRENT_TIMESTAMP "
            + "WHERE TRIM(guide_number) = TRIM(:guideNumber) AND status = 3",
            nativeQuery = true)
    int markShippingGuidesPendienteFacturar(@Param("guideNumber") String guideNumber);

    /**
     * Al pasar una factura de transporte a 17 (Pendiente de complemento, es decir pagada), las guías
     * ligadas por {@code guide_number} pasan a estatus 7 (Pagada). Solo desde estatus en el pipeline
     * contable (3 Por Contabilizar / 4 En proceso de contabilización / 5 Contabilizada) para no pisar
     * guías sin facturar (1/2), rechazadas (6) ni terminales (7/9/10).
     * Tabla de conversión de estatus v1.0(7) (Ivan): Factura 17 -> Carta Porte 7 (solo transporte).
     *
     * @return filas actualizadas (0 si no hay guía o no está en 3/4/5)
     */
    @Modifying(clearAutomatically = true)
    @Query(value =
            "UPDATE tenant_finance.shipping_guide "
            + "SET status = 7, is_status_updated = true, updated_at = CURRENT_TIMESTAMP "
            + "WHERE TRIM(guide_number) = TRIM(:guideNumber) AND status IN (3, 4, 5)",
            nativeQuery = true)
    int markShippingGuidesPagada(@Param("guideNumber") String guideNumber);

    /**
     * Mueve la(s) guía(s) ligadas por {@code guide_number} de un estatus {@code from} a {@code to}.
     * Solo actualiza las que están en {@code from} (no pisa otros estatus). Usado por la cascada
     * contable de Carta Porte al avanzar una factura de transporte (Ivan v1.0(9)):
     * factura 5 (Desglose) -> guía 3->4; factura 15 (Pendiente de Pago) -> guía 4->5.
     *
     * @return filas actualizadas (0 si no hay guía o no está en {@code from})
     */
    @Modifying(clearAutomatically = true)
    @Query(value =
            "UPDATE tenant_finance.shipping_guide "
            + "SET status = :to, is_status_updated = true, updated_at = CURRENT_TIMESTAMP "
            + "WHERE TRIM(guide_number) = TRIM(:guideNumber) AND status = :from",
            nativeQuery = true)
    int updateShippingGuideStatus(@Param("guideNumber") String guideNumber,
                                  @Param("from") int from, @Param("to") int to);
}

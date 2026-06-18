package com.sodimac.fiscal.api.repository.specification;

import com.sodimac.fiscal.api.model.dto.InvoiceSearchRequest;
import com.sodimac.fiscal.api.model.entity.AddendumEntity;
import com.sodimac.fiscal.api.model.entity.InvoiceEntity;
import com.sodimac.fiscal.api.model.entity.IssuerEntity;
import com.sodimac.fiscal.api.model.entity.ReceiverEntity;
import com.sodimac.fiscal.api.model.entity.ReceptionEntity;
import com.sodimac.fiscal.api.model.entity.RelatedCfdiEntity;
import jakarta.persistence.criteria.*;
import org.springframework.data.jpa.domain.Specification;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/**
 * Specification para búsqueda dinámica de facturas y notas de crédito (STM-338).
 *
 * Construye predicados dinámicamente según los filtros proporcionados:
 * - Obligatorios: RFC Emisor, Fecha Inicio/Fin, Tipo Documento
 * - Opcionales: RFC Receptor, ID Proveedor, Serie, Folio, UUID
 *
 * @author Sodimac Tech Team
 * @since 2025-11-10
 */
public class InvoiceSpecification {

    /**
     * Construye la Specification completa a partir del request de búsqueda.
     *
     * @param searchRequest Filtros de búsqueda
     * @return Specification para ejecutar la query
     */
    public static Specification<InvoiceEntity> buildSpecification(InvoiceSearchRequest searchRequest) {
        return buildSpecification(searchRequest, null, null);
    }

    public static Specification<InvoiceEntity> buildSpecification(InvoiceSearchRequest searchRequest, List<String> allowedVendors) {
        return buildSpecification(searchRequest, allowedVendors, null);
    }

    /**
     * @param receptionInvoiceUuids invoice_uuid que cumplen el filtro de fecha de recepción
     *        (precomputado en el servicio vía addendum→reception). {@code null} = no se pidió
     *        filtro de fecha; lista vacía = se pidió pero ninguna recepción cae en el rango
     *        (resultado vacío). Ver fix Fer 2026-06-18.
     */
    public static Specification<InvoiceEntity> buildSpecification(InvoiceSearchRequest searchRequest,
            List<String> allowedVendors, List<UUID> receptionInvoiceUuids) {
        return (root, query, criteriaBuilder) -> {
            List<Predicate> predicates = new ArrayList<>();

            // Join con Issuer para RFC Emisor
            Join<InvoiceEntity, IssuerEntity> issuerJoin = root.join("issuer", JoinType.INNER);

            // Join con Receiver para RFC Receptor (opcional)
            Join<InvoiceEntity, ReceiverEntity> receiverJoin = root.join("receiver", JoinType.LEFT);

            // ========== FILTROS OBLIGATORIOS ==========

            // 1. RFC Emisor (Obligatorio)
            if (searchRequest.getRfcEmisor() != null && !searchRequest.getRfcEmisor().isBlank()) {
                predicates.add(
                        criteriaBuilder.equal(
                                issuerJoin.get("rfc"),
                                searchRequest.getRfcEmisor().toUpperCase()
                        )
                );
            }

            // 2 + 3. Fecha de Recepción (Obligatorio)
            // FIX (Fer 2026-06-18): antes se filtraba por invoice.createdAt (fecha de REGISTRO),
            // que difiere hasta semanas de la fecha real de recepción. Ahora el servicio precomputa
            // los invoice_uuid cuya reception_date (tenant_finance.reception) cae en el rango,
            // vinculados vía addendum.reception_number = reception.reception_id, y se filtran aquí.
            // receptionInvoiceUuids: null = sin filtro; vacío = pedido pero ninguna recepción en rango.
            if (receptionInvoiceUuids != null) {
                if (receptionInvoiceUuids.isEmpty()) {
                    predicates.add(criteriaBuilder.disjunction()); // ninguna coincidencia -> 0 resultados
                } else {
                    predicates.add(root.get("invoiceUuid").in(receptionInvoiceUuids));
                }
            }

            // 4. Tipo de Documento (Obligatorio)
            if (searchRequest.getTipoDocumento() != null && !searchRequest.getTipoDocumento().isBlank()) {
                predicates.add(
                        criteriaBuilder.equal(
                                root.get("documentType"),
                                searchRequest.getTipoDocumento().toUpperCase()
                        )
                );
            }

            // ========== FILTROS OPCIONALES ==========

            // 5. RFC Receptor (Opcional)
            if (searchRequest.getRfcReceptor() != null && !searchRequest.getRfcReceptor().isBlank()) {
                predicates.add(
                        criteriaBuilder.equal(
                                receiverJoin.get("rfc"),
                                searchRequest.getRfcReceptor().toUpperCase()
                        )
                );
            }

            // 6. ID Proveedor / Supplier Number (Opcional) - Busca en Addenda
            if (searchRequest.getIdProveedor() != null) {
                Subquery<UUID> addendumSubquery = query.subquery(UUID.class);
                Root<AddendumEntity> addendumRoot = addendumSubquery.from(AddendumEntity.class);
                addendumSubquery.select(addendumRoot.get("invoiceUuid"))
                        .where(criteriaBuilder.equal(
                                addendumRoot.get("supplierNumber"),
                                searchRequest.getIdProveedor()
                        ));
                predicates.add(root.get("invoiceUuid").in(addendumSubquery));
            }

            // 7. Serie (Opcional)
            if (searchRequest.getSerie() != null && !searchRequest.getSerie().isBlank()) {
                predicates.add(
                        criteriaBuilder.equal(
                                root.get("series"),
                                searchRequest.getSerie().toUpperCase()
                        )
                );
            }

            // 8. Folio (Opcional)
            if (searchRequest.getFolio() != null && !searchRequest.getFolio().isBlank()) {
                predicates.add(
                        criteriaBuilder.equal(
                                root.get("folio"),
                                searchRequest.getFolio()
                        )
                );
            }

            // 9. UUID Fiscal (Opcional)
            if (searchRequest.getUuid() != null) {
                predicates.add(
                        criteriaBuilder.equal(
                                root.get("fiscalUuid"),
                                searchRequest.getUuid()
                        )
                );
            }

            // 10. Estatus (Opcional) - STM-771
            if (searchRequest.getEstatus() != null) {
                predicates.add(
                        criteriaBuilder.equal(
                                root.get("status"),
                                searchRequest.getEstatus()
                        )
                );
            }

            // 11. Numero de Orden de Compra (Opcional) - STM-338
            if (searchRequest.getNoOrdenCompra() != null && !searchRequest.getNoOrdenCompra().isBlank()) {
                Subquery<UUID> addendumSubquery = query.subquery(UUID.class);
                Root<AddendumEntity> addendumRoot = addendumSubquery.from(AddendumEntity.class);
                addendumSubquery.select(addendumRoot.get("invoiceUuid"))
                        .where(criteriaBuilder.equal(
                                addendumRoot.get("purchaseOrderNumber"),
                                searchRequest.getNoOrdenCompra()
                        ));
                predicates.add(root.get("invoiceUuid").in(addendumSubquery));
            }

            // 12. Numero de Recepcion (Opcional) - STM-338
            if (searchRequest.getNoRecepcion() != null && !searchRequest.getNoRecepcion().isBlank()) {
                Subquery<UUID> addendumSubquery = query.subquery(UUID.class);
                Root<AddendumEntity> addendumRoot = addendumSubquery.from(AddendumEntity.class);
                addendumSubquery.select(addendumRoot.get("invoiceUuid"))
                        .where(criteriaBuilder.equal(
                                addendumRoot.get("receptionNumber"),
                                searchRequest.getNoRecepcion()
                        ));
                predicates.add(root.get("invoiceUuid").in(addendumSubquery));
            }

            // 13. NCs relacionadas a una factura específica (Opcional) - STM-335
            if (searchRequest.getRelatedInvoiceUuid() != null) {
                Subquery<UUID> relatedCfdiSubquery = query.subquery(UUID.class);
                Root<RelatedCfdiEntity> relatedCfdiRoot = relatedCfdiSubquery.from(RelatedCfdiEntity.class);
                relatedCfdiSubquery.select(relatedCfdiRoot.get("invoiceUuid"))
                        .where(criteriaBuilder.equal(
                                relatedCfdiRoot.get("relatedInvoiceUuid"),
                                searchRequest.getRelatedInvoiceUuid()
                        ));
                predicates.add(root.get("invoiceUuid").in(relatedCfdiSubquery));
            }

            // Filtro de seguridad STM-323: supplier permitidos del BFF (x-user-vendors)
            if (allowedVendors != null && !allowedVendors.isEmpty()) {
                List<BigDecimal> vendorNumbers = allowedVendors.stream()
                        .map(v -> { try { return new BigDecimal(v); } catch (NumberFormatException e) { return null; } })
                        .filter(v -> v != null)
                        .toList();
                if (!vendorNumbers.isEmpty()) {
                    Subquery<UUID> secSubquery = query.subquery(UUID.class);
                    Root<AddendumEntity> secRoot = secSubquery.from(AddendumEntity.class);
                    secSubquery.select(secRoot.get("invoiceUuid"))
                            .where(secRoot.get("supplierNumber").in(vendorNumbers));
                    predicates.add(root.get("invoiceUuid").in(secSubquery));
                }
            }

            // Combinar todos los predicados con AND
            return criteriaBuilder.and(predicates.toArray(new Predicate[0]));
        };
    }

    /**
     * Specification para buscar por RFC Emisor.
     */
    public static Specification<InvoiceEntity> hasRfcEmisor(String rfcEmisor) {
        return (root, query, criteriaBuilder) -> {
            if (rfcEmisor == null || rfcEmisor.isBlank()) {
                return criteriaBuilder.conjunction();
            }
            Join<InvoiceEntity, IssuerEntity> issuerJoin = root.join("issuer", JoinType.INNER);
            return criteriaBuilder.equal(issuerJoin.get("rfc"), rfcEmisor.toUpperCase());
        };
    }

    /**
     * Specification para buscar por RFC Receptor.
     */
    public static Specification<InvoiceEntity> hasRfcReceptor(String rfcReceptor) {
        return (root, query, criteriaBuilder) -> {
            if (rfcReceptor == null || rfcReceptor.isBlank()) {
                return criteriaBuilder.conjunction();
            }
            Join<InvoiceEntity, ReceiverEntity> receiverJoin = root.join("receiver", JoinType.INNER);
            return criteriaBuilder.equal(receiverJoin.get("rfc"), rfcReceptor.toUpperCase());
        };
    }

    /**
     * Specification para buscar por tipo de documento.
     */
    public static Specification<InvoiceEntity> hasTipoDocumento(String tipoDocumento) {
        return (root, query, criteriaBuilder) -> {
            if (tipoDocumento == null || tipoDocumento.isBlank()) {
                return criteriaBuilder.conjunction();
            }
            return criteriaBuilder.equal(root.get("documentType"), tipoDocumento.toUpperCase());
        };
    }

    /**
     * Specification para buscar por rango de fechas de recepción.
     */
    public static Specification<InvoiceEntity> hasCreatedAtBetween(LocalDateTime from, LocalDateTime to) {
        return (root, query, criteriaBuilder) -> {
            if (from == null && to == null) {
                return criteriaBuilder.conjunction();
            }
            if (from != null && to != null) {
                return criteriaBuilder.between(root.get("createdAt"), from, to);
            }
            if (from != null) {
                return criteriaBuilder.greaterThanOrEqualTo(root.get("createdAt"), from);
            }
            return criteriaBuilder.lessThanOrEqualTo(root.get("createdAt"), to);
        };
    }
}

package com.sodimac.fiscal.api.repository;

import java.util.ArrayList;
import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Repository;

import com.sodimac.fiscal.api.model.dto.request.PaymentSearchRequest;
import com.sodimac.fiscal.api.model.entity.AddendumEntity;
import com.sodimac.fiscal.api.model.entity.IssuerEntity;
import com.sodimac.fiscal.api.model.entity.PaymentsEntity;
import com.sodimac.fiscal.api.model.entity.ReceiverEntity;

import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import jakarta.persistence.criteria.CriteriaBuilder;
import jakarta.persistence.criteria.CriteriaQuery;
import jakarta.persistence.criteria.Fetch;
import jakarta.persistence.criteria.Join;
import jakarta.persistence.criteria.JoinType;
import jakarta.persistence.criteria.Order;
import jakarta.persistence.criteria.Predicate;
import jakarta.persistence.criteria.Root;
import jakarta.persistence.criteria.Subquery;

import java.util.UUID;
import lombok.extern.slf4j.Slf4j;

/**
 * Implementación del repositorio custom para consultas dinámicas con JPA Criteria.
 *
 * Construye queries dinámicas basadas en los filtros proporcionados,
 * permitiendo búsquedas flexibles sin necesidad de crear múltiples
 * métodos de consulta en el repositorio.
 *
 * @author Sodimac Tech Team
 * @version 1.0
 * @since 2025
 */
@Repository
@Slf4j
public class PaymentsRepositoryCustomImpl implements PaymentsRepositoryCustom {

    private static final String K_ISSUER = "issuer";
    private static final String K_RECEIVER = "receiver";
    private static final String K_PAYMENTS_UUID = "paymentsUuid";
    private static final String K_FISCAL_UUID = "fiscalUuid";
    private static final String K_CREATED_AT = "createdAt";
    private static final String K_SUPPLIER_NUMBER = "supplierNumber";

    @PersistenceContext
    private EntityManager entityManager;

    @Override
    public Page<PaymentsEntity> searchPayments(PaymentSearchRequest searchRequest) {
        log.debug("Iniciando búsqueda de complementos de pago con criterios: {}", searchRequest);

        CriteriaBuilder cb = entityManager.getCriteriaBuilder();

        // Query para datos
        CriteriaQuery<PaymentsEntity> query = cb.createQuery(PaymentsEntity.class);
        Root<PaymentsEntity> root = query.from(PaymentsEntity.class);

        // Joins para issuer y receiver (fetch para evitar N+1)
        Fetch<PaymentsEntity, IssuerEntity> issuerFetch = root.fetch(K_ISSUER, JoinType.LEFT);
        Fetch<PaymentsEntity, ReceiverEntity> receiverFetch = root.fetch(K_RECEIVER, JoinType.LEFT);

        // Construir predicados dinámicamente
        List<Predicate> predicates = buildPredicates(cb, root, searchRequest,
                (Join<PaymentsEntity, IssuerEntity>) issuerFetch,
                (Join<PaymentsEntity, ReceiverEntity>) receiverFetch);
        addTipoProveedorPredicate(cb, query, root, searchRequest.getTipoProveedor(), predicates);
        addNumeroProveedorPredicate(cb, query, root, searchRequest.getNumeroProveedor(), predicates);

        // Aplicar predicados
        if (!predicates.isEmpty()) {
            query.where(cb.and(predicates.toArray(new Predicate[0])));
        }

        // Ordenamiento
        Sort.Direction direction = "DESC".equalsIgnoreCase(searchRequest.getSortDirection())
                ? Sort.Direction.DESC
                : Sort.Direction.ASC;
        Order order = direction == Sort.Direction.DESC
                ? cb.desc(root.get(searchRequest.getSortBy()))
                : cb.asc(root.get(searchRequest.getSortBy()));
        query.orderBy(order);

        // Ejecutar query con paginación
        List<PaymentsEntity> results = entityManager.createQuery(query)
                .setFirstResult(searchRequest.getPage() * searchRequest.getSize())
                .setMaxResults(searchRequest.getSize())
                .getResultList();

        // Query para contar total
        long total = countPayments(searchRequest);

        log.debug("Búsqueda completada. Resultados: {}, Total: {}", results.size(), total);

        return new PageImpl<>(
                results,
                PageRequest.of(searchRequest.getPage(), searchRequest.getSize()),
                total
        );
    }

    /**
     * Construye los predicados dinámicamente basándose en los filtros proporcionados.
     */
    private List<Predicate> buildPredicates(CriteriaBuilder cb, Root<PaymentsEntity> root,
                                           PaymentSearchRequest searchRequest,
                                           Join<PaymentsEntity, IssuerEntity> issuerJoin,
                                           Join<PaymentsEntity, ReceiverEntity> receiverJoin) {
        List<Predicate> predicates = new ArrayList<>();

        // UUID: acepta PK interno o folio fiscal SAT
        if (searchRequest.getPaymentsUuid() != null) {
            UUID uuid = searchRequest.getPaymentsUuid();
            predicates.add(cb.or(
                    cb.equal(root.get(K_PAYMENTS_UUID), uuid),
                    cb.equal(root.get(K_FISCAL_UUID), uuid)
            ));
        }

        // Filtro por folio
        if (searchRequest.getFolio() != null && !searchRequest.getFolio().isBlank()) {
            predicates.add(cb.equal(root.get("folio"), searchRequest.getFolio().trim()));
        }

        // Filtro por serie
        if (searchRequest.getSerie() != null && !searchRequest.getSerie().isBlank()) {
            predicates.add(cb.equal(root.get("series"), searchRequest.getSerie().trim()));
        }

        // Filtro por RFC emisor (usa join existente)
        if (searchRequest.getRfcEmisor() != null && !searchRequest.getRfcEmisor().isBlank()) {
            predicates.add(cb.equal(issuerJoin.get("rfc"), searchRequest.getRfcEmisor().trim().toUpperCase()));
        }

        // Filtro por RFC receptor (usa join existente)
        if (searchRequest.getRfcReceptor() != null && !searchRequest.getRfcReceptor().isBlank()) {
            predicates.add(cb.equal(receiverJoin.get("rfc"), searchRequest.getRfcReceptor().trim().toUpperCase()));
        }

        // Filtro por rango de fechas de pago
        if (searchRequest.getFechaPagoInicio() != null) {
            predicates.add(cb.greaterThanOrEqualTo(root.get("paymentDate"), searchRequest.getFechaPagoInicio()));
        }
        if (searchRequest.getFechaPagoFin() != null) {
            predicates.add(cb.lessThanOrEqualTo(root.get("paymentDate"), searchRequest.getFechaPagoFin()));
        }

        // Filtro por fecha de registro (created_at)
        if (searchRequest.getFechaRegistroInicio() != null) {
            predicates.add(cb.greaterThanOrEqualTo(
                    root.get(K_CREATED_AT),
                    searchRequest.getFechaRegistroInicio().atStartOfDay()));
        }
        if (searchRequest.getFechaRegistroFin() != null) {
            predicates.add(cb.lessThan(
                    root.get(K_CREATED_AT),
                    searchRequest.getFechaRegistroFin().plusDays(1).atStartOfDay()));
        }

        // Filtro por status
        if (searchRequest.getStatus() != null) {
            predicates.add(cb.equal(root.get("status"), searchRequest.getStatus()));
        }

        return predicates;
    }

    /**
     * Filtro por número de proveedor (addendum.supplier_number vía payments_uuid).
     */
    private void addNumeroProveedorPredicate(CriteriaBuilder cb, CriteriaQuery<?> query,
                                             Root<PaymentsEntity> root,
                                             Long numeroProveedor,
                                             List<Predicate> predicates) {
        if (numeroProveedor == null) {
            return;
        }
        Subquery<UUID> sub = query.subquery(UUID.class);
        Root<AddendumEntity> addRoot = sub.from(AddendumEntity.class);
        sub.select(addRoot.get(K_PAYMENTS_UUID))
                .where(cb.and(
                        cb.isNotNull(addRoot.get(K_PAYMENTS_UUID)),
                        cb.equal(addRoot.get(K_SUPPLIER_NUMBER), java.math.BigDecimal.valueOf(numeroProveedor))
                ));
        predicates.add(root.get(K_PAYMENTS_UUID).in(sub));
    }

    /**
     * Cuenta el total de registros que cumplen con los criterios de búsqueda.
     */
    private long countPayments(PaymentSearchRequest searchRequest) {
        CriteriaBuilder cb = entityManager.getCriteriaBuilder();
        CriteriaQuery<Long> countQuery = cb.createQuery(Long.class);
        Root<PaymentsEntity> root = countQuery.from(PaymentsEntity.class);

        // Joins necesarios para filtros de RFC
        Join<PaymentsEntity, IssuerEntity> issuerJoin = root.join(K_ISSUER, JoinType.LEFT);
        Join<PaymentsEntity, ReceiverEntity> receiverJoin = root.join(K_RECEIVER, JoinType.LEFT);

        // Construir los mismos predicados
        List<Predicate> predicates = buildPredicates(cb, root, searchRequest, issuerJoin, receiverJoin);
        addTipoProveedorPredicate(cb, countQuery, root, searchRequest.getTipoProveedor(), predicates);
        addNumeroProveedorPredicate(cb, countQuery, root, searchRequest.getNumeroProveedor(), predicates);

        countQuery.select(cb.count(root));

        if (!predicates.isEmpty()) {
            countQuery.where(cb.and(predicates.toArray(new Predicate[0])));
        }

        return entityManager.createQuery(countQuery).getSingleResult();
    }

    // -------------------------------------------------------------------------
    // STM-1474: overload con filtro de seguridad por vendor
    // -------------------------------------------------------------------------

    @Override
    public Page<PaymentsEntity> searchPayments(PaymentSearchRequest searchRequest, List<String> allowedVendors) {
        CriteriaBuilder cb = entityManager.getCriteriaBuilder();

        CriteriaQuery<PaymentsEntity> query = cb.createQuery(PaymentsEntity.class);
        Root<PaymentsEntity> root = query.from(PaymentsEntity.class);

        Fetch<PaymentsEntity, IssuerEntity> issuerFetch = root.fetch(K_ISSUER, JoinType.LEFT);
        Fetch<PaymentsEntity, ReceiverEntity> receiverFetch = root.fetch(K_RECEIVER, JoinType.LEFT);

        List<Predicate> predicates = buildPredicates(cb, root, searchRequest,
                (Join<PaymentsEntity, IssuerEntity>) issuerFetch,
                (Join<PaymentsEntity, ReceiverEntity>) receiverFetch);
        addTipoProveedorPredicate(cb, query, root, searchRequest.getTipoProveedor(), predicates);
        addNumeroProveedorPredicate(cb, query, root, searchRequest.getNumeroProveedor(), predicates);

        // Filtro de seguridad por vendor (addendum.supplier_number)
        addVendorPredicate(cb, query, root, allowedVendors, predicates);

        if (!predicates.isEmpty()) query.where(cb.and(predicates.toArray(new Predicate[0])));

        Sort.Direction direction = "DESC".equalsIgnoreCase(searchRequest.getSortDirection())
                ? Sort.Direction.DESC : Sort.Direction.ASC;
        query.orderBy(direction == Sort.Direction.DESC
                ? cb.desc(root.get(searchRequest.getSortBy()))
                : cb.asc(root.get(searchRequest.getSortBy())));

        List<PaymentsEntity> results = entityManager.createQuery(query)
                .setFirstResult(searchRequest.getPage() * searchRequest.getSize())
                .setMaxResults(searchRequest.getSize())
                .getResultList();

        long total = countPaymentsWithVendors(searchRequest, allowedVendors);

        return new PageImpl<>(results, PageRequest.of(searchRequest.getPage(), searchRequest.getSize()), total);
    }

    /**
     * Filtro por tipo de proveedor (issue Fer #5): subquery sobre addendum.supplier_type
     * (id 1-4 de CatTipoProveedor), vinculado por payments_uuid.
     */
    private void addTipoProveedorPredicate(CriteriaBuilder cb, CriteriaQuery<?> query,
                                           Root<PaymentsEntity> root,
                                           String tipoProveedor,
                                           List<Predicate> predicates) {
        if (tipoProveedor == null || tipoProveedor.isBlank()) return;
        Subquery<UUID> sub = query.subquery(UUID.class);
        Root<AddendumEntity> addRoot = sub.from(AddendumEntity.class);
        sub.select(addRoot.get(K_PAYMENTS_UUID))
           .where(cb.and(
               cb.isNotNull(addRoot.get(K_PAYMENTS_UUID)),
               cb.equal(addRoot.get("supplierType"), tipoProveedor)
           ));
        predicates.add(root.get(K_PAYMENTS_UUID).in(sub));
    }

    private void addVendorPredicate(CriteriaBuilder cb, CriteriaQuery<?> query,
                                    Root<PaymentsEntity> root,
                                    List<String> allowedVendors,
                                    List<Predicate> predicates) {
        if (allowedVendors == null || allowedVendors.isEmpty()) return;
        List<java.math.BigDecimal> vendorNumbers = allowedVendors.stream()
            .map(v -> new java.math.BigDecimal(v.trim()))
            .collect(java.util.stream.Collectors.toList());
        Subquery<UUID> sub = query.subquery(UUID.class);
        Root<AddendumEntity> addRoot = sub.from(AddendumEntity.class);
        sub.select(addRoot.get(K_PAYMENTS_UUID))
           .where(cb.and(
               cb.isNotNull(addRoot.get(K_PAYMENTS_UUID)),
               addRoot.get("supplierNumber").in(vendorNumbers)
           ));
        predicates.add(root.get(K_PAYMENTS_UUID).in(sub));
    }

    private long countPaymentsWithVendors(PaymentSearchRequest searchRequest, List<String> allowedVendors) {
        CriteriaBuilder cb = entityManager.getCriteriaBuilder();
        CriteriaQuery<Long> countQuery = cb.createQuery(Long.class);
        Root<PaymentsEntity> root = countQuery.from(PaymentsEntity.class);

        Join<PaymentsEntity, IssuerEntity> issuerJoin = root.join(K_ISSUER, JoinType.LEFT);
        Join<PaymentsEntity, ReceiverEntity> receiverJoin = root.join(K_RECEIVER, JoinType.LEFT);

        List<Predicate> predicates = buildPredicates(cb, root, searchRequest, issuerJoin, receiverJoin);
        addTipoProveedorPredicate(cb, countQuery, root, searchRequest.getTipoProveedor(), predicates);
        addNumeroProveedorPredicate(cb, countQuery, root, searchRequest.getNumeroProveedor(), predicates);
        addVendorPredicate(cb, countQuery, root, allowedVendors, predicates);

        countQuery.select(cb.count(root));
        if (!predicates.isEmpty()) countQuery.where(cb.and(predicates.toArray(new Predicate[0])));

        return entityManager.createQuery(countQuery).getSingleResult();
    }
}

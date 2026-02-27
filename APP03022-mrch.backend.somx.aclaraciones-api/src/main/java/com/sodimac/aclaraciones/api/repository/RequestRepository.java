package com.sodimac.aclaraciones.api.repository;

import com.sodimac.aclaraciones.api.model.entity.Request;
import java.util.Date;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;
import java.util.List;

import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

@Repository
public interface RequestRepository extends CrudRepository<Request, Integer> {

    List<Request> findByActiveAndCreationTimeGreaterThanAndCreationTimeLessThanAndRequesterEmail(
            boolean active,
            Date lowerTimeThreshold,
            Date upperTimeThreshold,
            String requesterEmail,
            PageRequest page);

    @Query("SELECT r FROM Request r " +
            "WHERE r.active = :active " +
            "AND r.creationTime > :lowerTimeThreshold " +
            "AND r.creationTime < :upperTimeThreshold " +
            "AND r.requester.email = :requesterEmail " +
            "AND (r.id = :requestId OR r.orderId LIKE :orderId)")
    List<Request> findByCriteria(
            int requestId,
            String orderId,
            boolean active,
            Date lowerTimeThreshold,
            Date upperTimeThreshold,
            String requesterEmail);

    List<Request> findByActiveAndCreationTimeGreaterThanAndCreationTimeLessThanAndReasonId(
            boolean active,
            Date lowerTimeThreshold,
            Date upperTimeThreshold,
            int reasonId,
            PageRequest page);

    List<Request> findByActiveAndCreationTimeGreaterThanAndCreationTimeLessThanAndClazz(
            boolean active,
            Date lowerTimeThreshold,
            Date upperTimeThreshold,
            int clazz,
            PageRequest page);

    @Query("SELECT r FROM Request r " +
            "WHERE r.active = :active " +
            "AND r.creationTime > :lowerTimeThreshold " +
            "AND r.creationTime < :upperTimeThreshold " +
            "AND r.reason.id = :reasonId " +
            "AND (r.id = :requestId OR r.orderId LIKE :orderId OR r.requester.email LIKE :requesterEmail)")
    List<Request> findByOperatorCriteria(
            int requestId,
            String orderId,
            String requesterEmail,
            boolean active,
            Date lowerTimeThreshold,
            Date upperTimeThreshold,
            int reasonId);

    Request findByIdAndActive(int id, boolean active);

    // =========================
    // NUEVOS MÉTODOS GLOBALES
    // =========================

    @Query("SELECT r FROM Request r " +
            "WHERE r.active = :active " +
            "AND r.creationTime > :lowerTimeThreshold " +
            "AND r.creationTime < :upperTimeThreshold " +
            "AND ( " +
            "     (:requestId > 0 AND r.id = :requestId) " +
            "     OR (LOWER(r.description) LIKE LOWER(:likeCriteria)) " +
            "     OR (r.orderId LIKE :likeCriteria) " +
            "    ) " +
            "ORDER BY r.creationTime DESC")
    List<Request> findByCriteriaGlobal(
            int requestId,
            String likeCriteria,
            boolean active,
            Date lowerTimeThreshold,
            Date upperTimeThreshold);

    @Query("SELECT r FROM Request r " +
            "WHERE r.active = :active " +
            "AND r.creationTime > :lowerTimeThreshold " +
            "AND r.creationTime < :upperTimeThreshold")
    List<Request> findByActiveAndCreationTimeBetweenGlobal(
            boolean active,
            Date lowerTimeThreshold,
            Date upperTimeThreshold,
            PageRequest page);

    List<Request> findByModule_Id(int moduleId);

    // ============================================================
    // 🔹 EXISTENTE: búsqueda por módulo sin paginación
    // ============================================================
    @Query("""
            SELECT r FROM Request r
            WHERE r.active = true
              AND r.module.id = :moduleId
              AND (
                  :criteria IS NULL
                  OR LOWER(r.orderId) LIKE LOWER(CONCAT('%', :criteria, '%'))
                  OR LOWER(r.description) LIKE LOWER(CONCAT('%', :criteria, '%'))
              )
              AND r.creationTime >= COALESCE(:dateFrom, r.creationTime)
              AND r.creationTime <= COALESCE(:dateTo,   r.creationTime)
              AND (:reason IS NULL OR r.reason.id = :reason)
              AND (:clazz  IS NULL OR r.clazz.id  = :clazz)
            ORDER BY r.creationTime DESC
            """)
    List<Request> findByModuleWithFilters(
            @Param("moduleId") int moduleId,
            @Param("criteria") String criteria,
            @Param("dateFrom") Date dateFrom,
            @Param("dateTo") Date dateTo,
            @Param("reason") Integer reason,
            @Param("clazz") Integer clazz);

    // ============================================================
    // ✅ NUEVOS MÉTODOS CON PAGINACIÓN REAL (para resolver)
    // ============================================================
    @Query("""
            SELECT r FROM Request r
            WHERE r.active = true
              AND r.module.id = :moduleId
              AND (
                  :criteria IS NULL
                  OR LOWER(r.orderId) LIKE LOWER(CONCAT('%', :criteria, '%'))
                  OR LOWER(r.description) LIKE LOWER(CONCAT('%', :criteria, '%'))
              )
              AND r.creationTime >= COALESCE(:dateFrom, r.creationTime)
              AND r.creationTime <= COALESCE(:dateTo, r.creationTime)
              AND (:reason IS NULL OR r.reason.id = :reason)
              AND (:clazz  IS NULL OR r.clazz.id  = :clazz)
            """)
    List<Request> findByModuleWithFiltersPaged(
            @Param("moduleId") int moduleId,
            @Param("criteria") String criteria,
            @Param("dateFrom") Date dateFrom,
            @Param("dateTo") Date dateTo,
            @Param("reason") Integer reason,
            @Param("clazz") Integer clazz,
            org.springframework.data.domain.Pageable pageable);

    // 🔹 Método para contar el total (sin paginación)
    @Query("""
            SELECT COUNT(r) FROM Request r
            WHERE r.active = true
              AND r.module.id = :moduleId
              AND (
                  :criteria IS NULL
                  OR LOWER(r.orderId) LIKE LOWER(CONCAT('%', :criteria, '%'))
                  OR LOWER(r.description) LIKE LOWER(CONCAT('%', :criteria, '%'))
              )
              AND r.creationTime >= COALESCE(:dateFrom, r.creationTime)
              AND r.creationTime <= COALESCE(:dateTo, r.creationTime)
              AND (:reason IS NULL OR r.reason.id = :reason)
              AND (:clazz  IS NULL OR r.clazz.id  = :clazz)
            """)
    long countByModuleWithFilters(
            @Param("moduleId") int moduleId,
            @Param("criteria") String criteria,
            @Param("dateFrom") Date dateFrom,
            @Param("dateTo") Date dateTo,
            @Param("reason") Integer reason,
            @Param("clazz") Integer clazz);

    // ======================================================================
    // ✅ NUEVOS MÉTODOS PARA PAGINAR /requests (vendor / admin / resolver)
    // ======================================================================

    @Query("""
            SELECT r FROM Request r
            WHERE r.active = true
              AND r.creationTime >= COALESCE(:dateFrom, r.creationTime)
              AND r.creationTime <= COALESCE(:dateTo, r.creationTime)
              AND (
                  :criteria IS NULL
                  OR LOWER(r.orderId) LIKE LOWER(CONCAT('%', :criteria, '%'))
                  OR LOWER(r.description) LIKE LOWER(CONCAT('%', :criteria, '%'))
                  OR CAST(r.id AS string) = :criteria
              )
              AND (:reason IS NULL OR r.reason.id = :reason)
              AND (:clazz  IS NULL OR r.clazz.id  = :clazz)
              AND r.requester.email = :requesterEmail
            """)
    List<Request> findPagedRequests(
            @Param("criteria") String criteria,
            @Param("dateFrom") Date dateFrom,
            @Param("dateTo") Date dateTo,
            @Param("reason") Integer reason,
            @Param("clazz") Integer clazz,
            @Param("requesterEmail") String requesterEmail,
            Pageable pageable);

    // ======================================================================
    // 🔹 Conteo total de requests (para totalPages)
    // ======================================================================
    @Query("""
            SELECT COUNT(r) FROM Request r
            WHERE r.active = true
              AND r.creationTime >= COALESCE(:dateFrom, r.creationTime)
              AND r.creationTime <= COALESCE(:dateTo, r.creationTime)
              AND (
                  :criteria IS NULL
                  OR LOWER(r.orderId) LIKE LOWER(CONCAT('%', :criteria, '%'))
                  OR LOWER(r.description) LIKE LOWER(CONCAT('%', :criteria, '%'))
                  OR CAST(r.id AS string) = :criteria
              )
              AND (:reason IS NULL OR r.reason.id = :reason)
              AND (:clazz  IS NULL OR r.clazz.id  = :clazz)
              AND r.requester.email = :requesterEmail
            """)
    long countRequests(
            @Param("criteria") String criteria,
            @Param("dateFrom") Date dateFrom,
            @Param("dateTo") Date dateTo,
            @Param("reason") Integer reason,
            @Param("clazz") Integer clazz,
            @Param("requesterEmail") String requesterEmail);

    @Query("""
                SELECT r FROM Request r
                WHERE r.active = true
                  AND r.module.id IN (:moduleIds)
                  AND (
                      :criteria IS NULL
                      OR LOWER(r.orderId) LIKE LOWER(CONCAT('%', :criteria, '%'))
                      OR LOWER(r.description) LIKE LOWER(CONCAT('%', :criteria, '%'))
                  )
                  AND r.creationTime >= COALESCE(:dateFrom, r.creationTime)
                  AND r.creationTime <= COALESCE(:dateTo, r.creationTime)
                  AND (:reason IS NULL OR r.reason.id = :reason)
                  AND (:clazz  IS NULL OR r.clazz.id  = :clazz)
                  AND (r.responsible IS NULL OR LOWER(r.responsible) = LOWER(:userEmail))
                ORDER BY r.creationTime DESC
            """)
    List<Request> findByModulesWithFiltersPaged(
            @Param("moduleIds") List<Integer> moduleIds,
            @Param("criteria") String criteria,
            @Param("dateFrom") Date dateFrom,
            @Param("dateTo") Date dateTo,
            @Param("reason") Integer reason,
            @Param("clazz") Integer clazz,
            @Param("userEmail") String userEmail,
            Pageable pageable);

    @Query("""
                SELECT COUNT(r) FROM Request r
                WHERE r.active = true
                  AND r.module.id IN (:moduleIds)
                  AND (
                      :criteria IS NULL
                      OR LOWER(r.orderId) LIKE LOWER(CONCAT('%', :criteria, '%'))
                      OR LOWER(r.description) LIKE LOWER(CONCAT('%', :criteria, '%'))
                  )
                  AND r.creationTime >= COALESCE(:dateFrom, r.creationTime)
                  AND r.creationTime <= COALESCE(:dateTo, r.creationTime)
                  AND (:reason IS NULL OR r.reason.id = :reason)
                  AND (:clazz  IS NULL OR r.clazz.id  = :clazz)
                  AND (r.responsible IS NULL OR LOWER(r.responsible) = LOWER(:userEmail))
            """)
    long countByModulesWithFilters(
            @Param("moduleIds") List<Integer> moduleIds,
            @Param("criteria") String criteria,
            @Param("dateFrom") Date dateFrom,
            @Param("dateTo") Date dateTo,
            @Param("reason") Integer reason,
            @Param("clazz") Integer clazz,
            @Param("userEmail") String userEmail);
}

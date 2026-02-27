package com.sodimac.catman.api.repository;

import java.time.LocalDate;
import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.sodimac.catman.api.model.entity.CatalogDetailRelation;

@Repository
public interface CatalogDetailRelationRepository extends JpaRepository<CatalogDetailRelation, Integer> {

    /**
     * Busca relaciones activas por ID del detalle origen.
     */
    List<CatalogDetailRelation> findBySourceDetailIdAndStatus(Integer sourceDetailId, Integer status);

    /**
     * Busca relaciones activas por ID del detalle destino.
     */
    List<CatalogDetailRelation> findByTargetDetailIdAndStatus(Integer targetDetailId, Integer status);

    /**
     * Busca relaciones por tipo.
     */
    List<CatalogDetailRelation> findByRelationTypeAndStatus(String relationType, Integer status);

    /**
     * Obtiene los IDs de detalles origen que dependen de un detalle destino específico.
     * Útil para: dado un RegimenFiscal, obtener los UsoCFDI que aplican.
     */
    @Query("SELECT r.sourceDetail.id FROM CatalogDetailRelation r " +
           "WHERE r.targetDetail.id = :targetDetailId " +
           "AND r.relationType = :relationType " +
           "AND r.status = :status")
    List<Integer> findSourceIdsByTargetAndType(
            @Param("targetDetailId") Integer targetDetailId,
            @Param("relationType") String relationType,
            @Param("status") Integer status);

    /**
     * Obtiene los IDs de detalles destino de los que depende un detalle origen.
     * Útil para: dado un UsoCFDI, obtener los RegimenFiscal compatibles.
     */
    @Query("SELECT r.targetDetail.id FROM CatalogDetailRelation r " +
           "WHERE r.sourceDetail.id = :sourceDetailId " +
           "AND r.relationType = :relationType " +
           "AND r.status = :status")
    List<Integer> findTargetIdsBySourceAndType(
            @Param("sourceDetailId") Integer sourceDetailId,
            @Param("relationType") String relationType,
            @Param("status") Integer status);

    /**
     * Obtiene detalles de un catálogo que dependen de un valor específico de otro catálogo.
     * Ejemplo: SELECT detalles de USO_CFDI que dependen del REGIMEN_FISCAL con external_key = '605'
     */
    @Query("SELECT r.sourceDetail FROM CatalogDetailRelation r " +
           "WHERE r.targetDetail.header.code = :targetCatalogCode " +
           "AND r.targetDetail.externalKey = :targetExternalKey " +
           "AND r.relationType = :relationType " +
           "AND r.status = :status " +
           "AND r.sourceDetail.status = :status")
    List<com.sodimac.catman.api.model.entity.CatalogDetail> findSourceDetailsByTargetCatalogAndKey(
            @Param("targetCatalogCode") String targetCatalogCode,
            @Param("targetExternalKey") String targetExternalKey,
            @Param("relationType") String relationType,
            @Param("status") Integer status);

    /**
     * Obtiene detalles de un catálogo que dependen de un valor específico de otro catálogo.
     * Incluye validación de vigencia (valid_from <= currentDate <= valid_to).
     * Ejemplo: SELECT detalles de USO_CFDI que dependen del REGIMEN_FISCAL con external_key = '605'
     */
    @Query("SELECT r.sourceDetail FROM CatalogDetailRelation r " +
           "WHERE r.targetDetail.header.code = :targetCatalogCode " +
           "AND r.targetDetail.externalKey = :targetExternalKey " +
           "AND r.relationType = :relationType " +
           "AND r.status = :status " +
           "AND r.sourceDetail.status = :status " +
           "AND (r.sourceDetail.validFrom IS NULL OR r.sourceDetail.validFrom <= :currentDate) " +
           "AND (r.sourceDetail.validTo IS NULL OR r.sourceDetail.validTo >= :currentDate)")
    List<com.sodimac.catman.api.model.entity.CatalogDetail> findSourceDetailsByTargetCatalogAndKeyAndValidDate(
            @Param("targetCatalogCode") String targetCatalogCode,
            @Param("targetExternalKey") String targetExternalKey,
            @Param("relationType") String relationType,
            @Param("status") Integer status,
            @Param("currentDate") LocalDate currentDate);

    /**
     * Verifica si existe una relación específica.
     */
    boolean existsBySourceDetailIdAndTargetDetailIdAndRelationType(
            Integer sourceDetailId, Integer targetDetailId, String relationType);
}

package com.sodimac.catman.api.repository;

import com.sodimac.catman.api.model.entity.CatalogConversion;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import java.util.List;
import java.util.Optional;

@Repository
public interface CatalogConversionRepository extends JpaRepository<CatalogConversion, Integer>,
        JpaSpecificationExecutor<CatalogConversion> {

    List<CatalogConversion> findBySourceElementId(Integer sourceElementId);

    Optional<CatalogConversion> findBySourceElementIdAndIsPrincipalTrue(Integer sourceElementId);

    @Modifying
    @Query("UPDATE CatalogConversion c SET c.isPrincipal = false, c.status = 0, c.updatedBy = :userId WHERE c.sourceElement.id = :sourceElementId AND c.isPrincipal = true")
    void clearPrincipalBySourceElement(@Param("sourceElementId") Integer sourceElementId, @Param("userId") String userId);

    boolean existsBySourceElementIdAndTargetElementId(Integer sourceElementId, Integer targetElementId);
}


package com.sodimac.batch.fiscal.download.repository.batch;

import com.sodimac.batch.fiscal.download.model.entity.batch.CtrlProcesoDetEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface CtrlProcesoDetRepository extends JpaRepository<CtrlProcesoDetEntity, Integer> {
}

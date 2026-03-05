package com.sodimac.batch.process.repository.batch;

import com.sodimac.batch.process.model.entity.batch.CtrlProcesoElementoEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface CtrlProcesoElementoRepository extends JpaRepository<CtrlProcesoElementoEntity, Integer> {
}

package com.sodimac.batch.process.repository.batch;

import com.sodimac.batch.process.model.entity.batch.CtrlProcesoCabEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface CtrlProcesoCabRepository extends JpaRepository<CtrlProcesoCabEntity, Integer> {
}

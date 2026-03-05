package com.sodimac.batch.process.repository.batch;

import com.sodimac.batch.process.model.entity.batch.CtrlLogEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface CtrlLogRepository extends JpaRepository<CtrlLogEntity, Integer> {
}

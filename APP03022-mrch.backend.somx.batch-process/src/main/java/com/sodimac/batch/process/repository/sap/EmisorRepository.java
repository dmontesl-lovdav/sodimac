package com.sodimac.batch.process.repository.sap;

import com.sodimac.batch.process.model.entity.sap.EmisorEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface EmisorRepository extends JpaRepository<EmisorEntity, Integer> {
}

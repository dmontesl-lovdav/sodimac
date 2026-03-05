package com.sodimac.batch.process.repository.sap;

import com.sodimac.batch.process.model.entity.sap.ConceptoEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ConceptoRepository extends JpaRepository<ConceptoEntity, Integer> {
}

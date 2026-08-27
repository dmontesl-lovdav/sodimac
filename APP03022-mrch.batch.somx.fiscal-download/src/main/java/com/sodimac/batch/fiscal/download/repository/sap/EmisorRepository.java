package com.sodimac.batch.fiscal.download.repository.sap;

import com.sodimac.batch.fiscal.download.model.entity.sap.EmisorEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface EmisorRepository extends JpaRepository<EmisorEntity, String> {
}

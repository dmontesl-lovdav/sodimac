package com.sodimac.batch.fiscal.download.repository.sap;

import com.sodimac.batch.fiscal.download.model.entity.sap.AddendaEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface AddendaRepository extends JpaRepository<AddendaEntity, String> {
}

package com.sodimac.batch.process.repository.sap;

import com.sodimac.batch.process.model.entity.sap.ImpuestosEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ImpuestosRepository extends JpaRepository<ImpuestosEntity, Integer> {
}

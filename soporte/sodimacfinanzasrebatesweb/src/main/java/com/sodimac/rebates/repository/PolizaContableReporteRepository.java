package com.sodimac.rebates.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.sodimac.rebates.model.PolizaContableReporteEntity;

@Repository
public interface PolizaContableReporteRepository extends JpaRepository<PolizaContableReporteEntity, String>{

}

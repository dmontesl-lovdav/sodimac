package com.sodimac.rebates.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.sodimac.rebates.model.PolizaContableEntity;

@Repository
public interface PolizaContableRepository extends JpaRepository<PolizaContableEntity, String>{
	
}

package com.sodimac.rebates.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import com.sodimac.rebates.model.CalculoRebateMSIEntity;

@Repository
public interface CalculoRebateMSIRepository  extends JpaRepository<CalculoRebateMSIEntity, Integer> {

	@Query(nativeQuery = true, value = "SELECT * FROM CalculoRebateMSIEntity")
	public List<CalculoRebateMSIEntity> findByIdPeriodo();
	
}

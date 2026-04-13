package com.sodimac.rebates.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import com.sodimac.rebates.model.CalculoRebateMSI3Entity;

@Repository
public interface CalculoRebateMSI3Repository  extends JpaRepository<CalculoRebateMSI3Entity, Integer> {

	@Query(nativeQuery = true, value = "SELECT * FROM CalculoRebateMSI3Entity")
	public List<CalculoRebateMSI3Entity> findByIdPeriodo();
	
}

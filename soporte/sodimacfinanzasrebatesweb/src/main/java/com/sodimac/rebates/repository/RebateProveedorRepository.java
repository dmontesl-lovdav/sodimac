package com.sodimac.rebates.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.sodimac.rebates.model.entity.RebateProveedorEntity;

@Repository
public interface RebateProveedorRepository extends JpaRepository<RebateProveedorEntity, Integer> {

	public RebateProveedorEntity findByCodigoProveedor(String codigoProveedor);
	
}

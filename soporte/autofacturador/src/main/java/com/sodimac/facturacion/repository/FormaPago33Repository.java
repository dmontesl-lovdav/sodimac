package com.sodimac.facturacion.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.sodimac.facturacion.entity.FacturasEntity;

@Repository
public interface FormaPago33Repository extends JpaRepository<FacturasEntity, Integer> {

	@Query(value = "{call uspExisteFormaPago33 (:idFormaPago)}", nativeQuery = true)	
	public int existeFormaPago33(@Param("idFormaPago") int idFormaPago);
	
}

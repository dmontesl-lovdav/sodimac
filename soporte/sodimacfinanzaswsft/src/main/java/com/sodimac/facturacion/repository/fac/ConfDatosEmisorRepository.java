package com.sodimac.facturacion.repository.fac;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.sodimac.facturacion.entity.fac.ConfDatosEmisorEntity;

@Repository("confDatosEmisorRepository")
public interface ConfDatosEmisorRepository extends JpaRepository<ConfDatosEmisorEntity, Integer> {

	@Query(value = "{call uspObtenerEmisorActivo ()}", nativeQuery = true)
	ConfDatosEmisorEntity findRfcActivo();	

	@Query(value = "{call uspEsFormaPagoBancaria (:formaPago)}", nativeQuery = true)	
	int esFormaPagoBancaria(@Param("formaPago") String formaPago);	

}
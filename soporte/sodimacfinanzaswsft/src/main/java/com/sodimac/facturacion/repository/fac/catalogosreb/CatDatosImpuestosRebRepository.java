package com.sodimac.facturacion.repository.fac.catalogosreb;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import com.sodimac.facturacion.entity.fac.catalogosreb.CatDatosImpuestosRebEntity;

@Repository("catDatosImpuestosRebRepository")
public interface CatDatosImpuestosRebRepository extends JpaRepository<CatDatosImpuestosRebEntity, Integer> {

	@Query(value = "{call uspObtenerDatosImpuestoRebActivo ()}", nativeQuery = true)
	CatDatosImpuestosRebEntity findRfcActivo();	

}

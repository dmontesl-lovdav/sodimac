package com.sodimac.facturacion.repository.fac.catalogosreb;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import com.sodimac.facturacion.entity.fac.catalogosreb.CatDatosConceptosRebEntity;

@Repository("catDatosConceptosRebRepository")
public interface CatDatosConceptosRebRepository extends JpaRepository<CatDatosConceptosRebEntity, Integer> {

	@Query(value = "{call uspObtenerDatosConceptoRebActivo ()}", nativeQuery = true)
	CatDatosConceptosRebEntity findRfcActivo();	

}

package com.sodimac.facturacion.repository.fac.catalogosreb;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import com.sodimac.facturacion.entity.fac.catalogosreb.ConfDatosEmisorRebEntity;

@Repository("confDatosEmisorRebRepository")
public interface ConfDatosEmisorRebRepository extends JpaRepository<ConfDatosEmisorRebEntity, Integer> {

	@Query(value = "{call uspObtenerEmisorRebActivo ()}", nativeQuery = true)
	ConfDatosEmisorRebEntity findRfcActivo();	

	@Query(value = "{call uspObtenerFolioReb ()}", nativeQuery = true)
	int findFolio();	
}

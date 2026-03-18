package com.sodimac.facturacion.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.sodimac.facturacion.entity.CatErrorSatEntity;

@Repository
public interface CatErrorSatRepository extends JpaRepository<CatErrorSatEntity, Integer> {

	@Query(value = "{call uspExisteErrorCatalogoSat (:pTicket)}", nativeQuery = true)	
	public String obtenerMensajeErrorSat(@Param("pTicket") String pTicket);
}

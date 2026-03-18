package com.sodimac.facturacion.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.sodimac.facturacion.entity.FacturasEntity;


@Repository("tiendas40Repository")
public interface Tiendas40Repository extends JpaRepository<FacturasEntity, Integer> {
	
	@Query(value = "{call uspExisteTienda40 (:tienda, :idAplicacion)}", nativeQuery = true)	
	int existeTienda(@Param("tienda") int tienda, @Param("idAplicacion") int idAplicacion);
	
}

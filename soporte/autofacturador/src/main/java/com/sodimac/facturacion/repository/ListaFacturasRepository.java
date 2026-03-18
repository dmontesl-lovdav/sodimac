package com.sodimac.facturacion.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.sodimac.facturacion.entity.ListaFacturasEntity;

@Repository("listaFacturasRepository")
public interface ListaFacturasRepository extends JpaRepository<ListaFacturasEntity, Integer> {

	
	@Query(value = "{call uspObtenerItemListaFactura (:ticket, :sessionId)}", nativeQuery = true)	
	ListaFacturasEntity getListaFacturaByTicket (@Param("ticket") String ticket, @Param("sessionId") String sessionId);
	
	@Query(value = "{call uspObtenerListaFacturaBySessionId (:sessionId)}", nativeQuery = true)
	List <ListaFacturasEntity> getListaFacturasBySessionId (@Param("sessionId") String sessionId);
	
}

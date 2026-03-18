package com.sodimac.facturacion.repository.fac;

import java.math.BigDecimal;
import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.sodimac.facturacion.entity.fac.ListaFacturasEntity;

@Repository("listaFacturasRepository")
public interface ListaFacturasRepository extends JpaRepository<ListaFacturasEntity, Integer> {

	@Query(value = "{call uspInsertarTotalTicket (:ticket, :total, :status, :sessionId)}", nativeQuery = true)	
	int saveEntity (@Param("ticket") String ticket, @Param("total") BigDecimal total, @Param("status") int status, @Param("sessionId") String sessionId);
	
	@Query(value = "{call uspObtenerItemListaFactura (:ticket, :sessionId)}", nativeQuery = true)	
	ListaFacturasEntity getListaFacturaByTicket (@Param("ticket") String ticket, @Param("sessionId") String sessionId);
	
	@Query(value = "{call uspObtenerListaFacturaBySessionId (:sessionId)}", nativeQuery = true)
	List <ListaFacturasEntity> getListaFacturasBySessionId (@Param("sessionId") String sessionId);
	
	ListaFacturasEntity findTop1ByTicketAndTotalOrderByFechaIngresoDesc(String ticket, BigDecimal total);
	
	ListaFacturasEntity findTop1ByTicketOrderByFechaIngresoDesc(String ticket);
	
}

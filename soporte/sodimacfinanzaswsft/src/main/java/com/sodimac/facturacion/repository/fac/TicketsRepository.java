package com.sodimac.facturacion.repository.fac;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.sodimac.facturacion.entity.fac.CatConfiguracionEntity;

@Repository("ticketsRepository")
public interface TicketsRepository extends JpaRepository<CatConfiguracionEntity, String> {

	@Query(value = "{call uspObtenerEstatusTicket (:ticket)}", nativeQuery = true)	
	int getEstatusTicket(@Param("ticket") String ticket);

	@Query(value = "{call uspEliminarTicket (:ticket)}", nativeQuery = true)	
	int eliminarTicket(@Param("ticket") String ticket);

	@Query(value = "{call uspExistTicketEnProceso (:ticket)}", nativeQuery = true)	
	int existTicketEnProceso(@Param("ticket") String ticket);
	
}
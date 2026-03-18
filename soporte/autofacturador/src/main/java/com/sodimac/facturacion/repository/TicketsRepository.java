package com.sodimac.facturacion.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.sodimac.facturacion.entity.CatConfiguracionEntity;

@Repository("ticketsRepository")
public interface TicketsRepository extends JpaRepository<CatConfiguracionEntity, Integer> {

	@Query(value = "{call uspObtenerEstatusTicket (:ticket)}", nativeQuery = true)	
	int getEstatusTicket(@Param("ticket") String ticket);
}
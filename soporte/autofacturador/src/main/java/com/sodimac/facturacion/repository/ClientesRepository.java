package com.sodimac.facturacion.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.sodimac.facturacion.entity.ClientesEntity;

@Repository("clientesRepository")
public interface ClientesRepository extends JpaRepository<ClientesEntity, Integer> {

	ClientesEntity findByRfc(String rfc);

	@Query(value = "{call uspExistRfcFactura (:rfc)}", nativeQuery = true)	
	int existRfcFactura(@Param("rfc") String rfc);	

	@Query(value = "{call uspInicializarRfcTicket (:rfc, :ticket)}", nativeQuery = true)	
	int inicializarRfcTicket(@Param("rfc") String rfc, @Param("ticket") String ticket);
}

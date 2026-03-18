package com.sodimac.facturacion.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.sodimac.facturacion.entity.ClientesTemporalEntity;

@Repository("clientesTemporalRepository")
public interface ClientesTemporalRepository extends JpaRepository<ClientesTemporalEntity, Integer> {
	
	ClientesTemporalEntity findTop1ByRfcOrderByFechaCreacionDesc(String rfc);

}

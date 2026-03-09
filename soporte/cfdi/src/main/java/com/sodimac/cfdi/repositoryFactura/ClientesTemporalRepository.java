package com.sodimac.cfdi.repositoryFactura;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.sodimac.cfdi.entityFactura.ClientesTemporalEntity;

@Repository("clientesTemporalRepository")
public interface ClientesTemporalRepository extends JpaRepository<ClientesTemporalEntity, Integer> {
	
	ClientesTemporalEntity findTop1ByRfcOrderByFechaCreacionDesc(String rfc);

}

package com.sodimac.cfdi.repositoryFactura;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.sodimac.cfdi.entityFactura.ClientesEntity;

@Repository("clientesFacturaRepository")
public interface ClientesFacturaRepository extends JpaRepository<ClientesEntity, Integer> {

	ClientesEntity findByRfc(String rfc);

}

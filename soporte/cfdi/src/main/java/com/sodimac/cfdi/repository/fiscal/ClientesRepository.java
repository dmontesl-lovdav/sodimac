package com.sodimac.cfdi.repository.fiscal;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.sodimac.cfdi.entity.fiscal.ClientesEntity;

@Repository("clientesRepository")
public interface ClientesRepository extends JpaRepository<ClientesEntity, Integer> {

	public ClientesEntity findByRfc(String rfc);
	
}

package com.sodimac.cfdi.repositoryFactura;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.sodimac.cfdi.entityFactura.FacturasEntity;

@Repository("facturasFacturaRepository")
public interface FacturasFacturaRepository extends JpaRepository<FacturasEntity, Integer> {

	FacturasEntity findByUuid(String uuid);
	
}

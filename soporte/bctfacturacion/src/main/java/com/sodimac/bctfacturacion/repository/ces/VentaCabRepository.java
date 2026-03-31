package com.sodimac.bctfacturacion.repository.ces;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import com.sodimac.bctfacturacion.entity.ces.VentaCabEntity;

@Repository
public interface VentaCabRepository extends JpaRepository<VentaCabEntity, Integer> {

	@Query(value = "SELECT NEXT VALUE FOR SQ_CES_VENTA_CAB", nativeQuery = true)
	public Integer getIdVentaCab();
	
	public long countByTicket(String ticket);
	
	public VentaCabEntity findByTicket(String ticket);
	
}

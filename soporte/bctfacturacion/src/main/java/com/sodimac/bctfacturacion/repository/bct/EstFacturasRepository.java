package com.sodimac.bctfacturacion.repository.bct;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.sodimac.bctfacturacion.entity.bct.EstFcturasEntity;

@Repository
public interface EstFacturasRepository extends JpaRepository<EstFcturasEntity, String> {

	@Query(value = "SELECT COUNT(1) "
			+ " FROM EST_FACTURAS "
			+ " WHERE NUM_FACTURA = :pUuid ", nativeQuery = true)
	public Integer existeTicket(@Param("pUuid") String pUuid);
	
	
	@Query(value = "SELECT NUM_FACTURA"
			+ " FROM EST_FACTURAS "
			+ " WHERE NUM_FACTURA IN (:pUuid)", nativeQuery = true)
	public List<String> getTickets(@Param("pUuid") List<String> pUuid);
	
}

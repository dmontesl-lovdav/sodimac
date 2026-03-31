package com.sodimac.bctfacturacion.repository.ces;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import com.sodimac.bctfacturacion.entity.ces.VentaDetImpuestoEntity;

@Repository
public interface VentaDetImpuestoRepository extends JpaRepository<VentaDetImpuestoEntity, Integer> {

	@Query(value = "SELECT NEXT VALUE FOR SQ_CES_DET_IMPUESTO", nativeQuery = true)
	public Integer getIdVentaDetImpuesto();
	
	public long countByTicketAndNumLinea(String ticket, Integer numLinea);
	
	public List<VentaDetImpuestoEntity> findByTicket(String ticket);
	
}

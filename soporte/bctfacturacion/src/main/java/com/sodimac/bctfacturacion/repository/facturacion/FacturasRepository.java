package com.sodimac.bctfacturacion.repository.facturacion;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.sodimac.bctfacturacion.entity.facturacion.FacturasEntity;

@Repository
public interface FacturasRepository extends JpaRepository<FacturasEntity, Integer> {

	@Query(value = "{call uspObtenerFacturaTicket (:ticket)}", nativeQuery = true)
	public FacturasEntity findByTicket(@Param("ticket") String ticket);
	
	@Query(value = "{call uspObtenerFacturaTicketFecha(:pFecha, :pNextDay)}", nativeQuery = true)
	public List<FacturasEntity> findByTicketByDate(@Param("pFecha") String pFecha
												 , @Param("pNextDay") String pNextDay);
	
}

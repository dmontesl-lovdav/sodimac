package com.sodimac.facturacion.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.sodimac.facturacion.entity.FacturasEntity;

@Repository("facturasRepository")
public interface FacturasRepository extends JpaRepository<FacturasEntity, Integer> {

	@Query(value = "{call uspExistFactura (:rfc, :ticket)}", nativeQuery = true)	
	int existFactura(@Param("rfc") String rfc, @Param("ticket") String ticket);
	
	@Query(value = "{call uspExistFactura ('', :ticket)}", nativeQuery = true)	
	int existFactura(@Param("ticket") String ticket);
	
	@Query(value = "{call uspObtenerFacturasRangoFechas (:rfc, :email, :fechaInicial, :fechaFinal, :start, :rowsPerPage)}", nativeQuery = true)	
	List<Object[]> getFacturasFechas(@Param("rfc") String rfc
			, @Param("email") String email
			, @Param("fechaInicial") String fechaInicial
			, @Param("fechaFinal") String fechaFinal
			, @Param("start") int start
			, @Param("rowsPerPage") int rowsPerPage);
	
	@Query(value = "{call uspCountFacturasRangoFechas (:rfc, :email, :fechaInicial, :fechaFinal)}", nativeQuery = true)	
	int getCountFacturas(@Param("rfc") String rfc
			, @Param("email") String email
			, @Param("fechaInicial") String fechaInicial
			, @Param("fechaFinal") String fechaFinal);
	
	FacturasEntity findByUuid(String uuid);
	
	@Query(value = "{call uspObtenerDatosFactura (:rfc,:sessionId, :ticket)}", nativeQuery = true)	
	List<Object[]> obtenerDatosFactura(@Param("rfc") String rfc, @Param("sessionId") String sessionId, @Param("ticket") String ticket);
	
	@Query(value = "{call uspObtenerDatosFactura (:rfc, :ticket)}", nativeQuery = true)	
	List<Object[]> obtenerDatosFactura(@Param("rfc") String rfc, @Param("ticket") String ticket);
	
	@Query(value = "{call uspObtenerFacturaTicket (:ticket)}", nativeQuery = true)
	FacturasEntity findByTicket(@Param("ticket") String ticket);
	
	List<FacturasEntity> findByRfcAndEmail(String rfc, String email);

	@Query(value = "{call uspInsertarLogFactura (:rfc, :ticket, :email, :pac, :idFacturaPac, :idEstatusFactura)}", nativeQuery = true)	
	int insertarLogFacturacion(@Param("rfc") String rfc
			, @Param("ticket") String ticket
			, @Param("email") String email
			, @Param("pac") int pac
			, @Param("idFacturaPac") int idFacturaPac
			, @Param("idEstatusFactura") int idEstatusFactura
			);

	@Query(value = "{call uspEliminarTicket(:ticket)}", nativeQuery = true)	
	public void liberarTicket(@Param("ticket") String ticket);

	
}

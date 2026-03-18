package com.sodimac.facturacion.repository.fac;

import java.math.BigDecimal;
import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.sodimac.facturacion.entity.fac.FacturasEntity;

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

	@Query(value = "{call uspCrearFactura (:rfc, :ticket, :razonSocial, :usoCfdi, :email, :autorizoGuardado, :pac, :idFacturaPac, :uuid, :fechaTimbrado, :versionFacturacionSat, :xml, :fechaCompra, :idEstatusFactura, :ticketBct, :versionFactura, :transaccion, :nombreObra, :responsableObra, :idComprobante, :uuidRelacionado, :serie, :folio, :subTotal, :total, :idOrigen, :metodoPago, :longitud, :latitud, :pagina, :explorador, :sistemaOperativo, :ip)}", nativeQuery = true)	
	int insertarFactura(@Param("rfc") String rfc
			, @Param("ticket") String ticket
			, @Param("razonSocial") String razonSocial
			, @Param("usoCfdi") Integer usoCfdi
			, @Param("email") String email
			, @Param("autorizoGuardado") int autorizoGuardado
			, @Param("pac") int pac
			, @Param("idFacturaPac") int idFacturaPac
			, @Param("uuid") String uuid
			, @Param("fechaTimbrado") String fechaTimbrado
			, @Param("versionFacturacionSat") String versionFacturacionSat
			, @Param("xml") String xml
			, @Param("fechaCompra") String fechaCompra
			, @Param("idEstatusFactura") int idEstatusFactura
			, @Param("ticketBct") String ticketBct
			, @Param("versionFactura") String versionFactura
			, @Param("transaccion") String transaccion
			, @Param("nombreObra") String nombreObra
			, @Param("responsableObra") String responsableObra
			, @Param("idComprobante") String idComprobante
			, @Param("uuidRelacionado") String uuidRelacionado
			, @Param("serie") String serie
			, @Param("folio") int folio
			, @Param("subTotal") BigDecimal subTotal
			, @Param("total") BigDecimal total
			, @Param("idOrigen") int idOrigen
			, @Param("metodoPago") String metodoPago
			, @Param("longitud") String longitud
			, @Param("latitud") String latitud
			, @Param("pagina") String pagina
			, @Param("explorador") String explorador
			, @Param("sistemaOperativo") String sistemaOperativo
			, @Param("ip") String ip
			);
		
	FacturasEntity findByUuid(String uuid);
	
	@Query(value = "{call uspObtenerDatosFactura (:rfc,:sessionId, :ticket)}", nativeQuery = true)	
	List<Object[]> obtenerDatosFactura(@Param("rfc") String rfc, @Param("sessionId") String sessionId, @Param("ticket") String ticket);
	
	@Query(value = "{call uspObtenerDatosFactura (:rfc, :ticket)}", nativeQuery = true)	
	List<Object[]> obtenerDatosFactura(@Param("rfc") String rfc, @Param("ticket") String ticket);
	
	@Query(value = "{call uspObtenerFacturaTicket (:ticket)}", nativeQuery = true)
	FacturasEntity findByTicket(@Param("ticket") String ticket);
	
	List<FacturasEntity> findByRfcAndEmail(String rfc, String email);

	@Query(value = "{call uspObtenerFacturaIdPac (:facturaId)}", nativeQuery = true)
	FacturasEntity findByFacturaIdPac(@Param("facturaId") String facturaId);

	@Query(value = "{call uspActualizarAcuse (:idFacturaPac, :acuse)}", nativeQuery = true)	
	int actualizarAcuse(@Param("idFacturaPac") int idFacturaPac, @Param("acuse") String acuse);

	@Query(value = "{call uspActualizarReintentos (:contadorid, :valorestatus, :rfc, :ticket)}", nativeQuery = true)
	public int actualizarReintentos(@Param("contadorid") int contadorid, @Param("valorestatus") int valorestatus, @Param("rfc") String rfc, @Param("ticket") String ticket);
	
	@Query(value = "{call uspInsertarLogFactura (:rfc, :ticket, :email, :pac, :idFacturaPac, :idEstatusFactura)}", nativeQuery = true)	
	int insertarLogFacturacion(@Param("rfc") String rfc
			, @Param("ticket") String ticket
			, @Param("email") String email
			, @Param("pac") int pac
			, @Param("idFacturaPac") int idFacturaPac
			, @Param("idEstatusFactura") int idEstatusFactura
			);
	
	@Query(value = "{call uspActualizaContadorTraerPac (:fechaTimbrado)}", nativeQuery = true)	
	int actualizaContadorTraerPac(@Param("fechaTimbrado") String fechaTimbrado);
	
	@Query(value = "{call uspInsertarFacturaMail (:pIdFacturaPac)}", nativeQuery = true)	
	int insertarFacturaMail(@Param("pIdFacturaPac") Integer pIdFacturaPac);
	
	@Query(value = "{call uspActualizaFacturaMail (:pIdFacturaPac, :pEstatus)}", nativeQuery = true)	
	int actualizarFacturaMail(@Param("pIdFacturaPac") Integer pIdFacturaPac
			              , @Param("pEstatus") Integer pEstatus);
	
}

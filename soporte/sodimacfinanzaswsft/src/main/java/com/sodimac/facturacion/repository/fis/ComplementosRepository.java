package com.sodimac.facturacion.repository.fis;

import java.math.BigDecimal;
import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.sodimac.facturacion.entity.fac.FacturasEntity;
import com.sodimac.facturacion.entity.fis.ComplementosEntity;

@Repository("complementosRepository")
public interface ComplementosRepository extends JpaRepository<ComplementosEntity, Integer> {

	@Query(value = "{call uspObtenerFacturasPago (:idTransaccionPago)}", nativeQuery = true)	
	List<Object[]> obtenerFacturas(@Param("idTransaccionPago") int idTransaccionPago);

	@Query(value = "{call uspObtenerPagoComp (:idTransaccionPago)}", nativeQuery = true)	
	List<Object[]> obtenerPagoComp(@Param("idTransaccionPago") int idTransaccionPago);
	
	@Query(value = "{call uspCrearComp (:rfc, :ticket, :razonSocial, :usoCfdi, :email, :autorizoGuardado, :pac, :idFacturaPac, :uuid, :fechaTimbrado, :versionFacturacionSat, :xml, :fechaCompra, :idEstatusFactura, :ticketBct, :versionFactura, :transaccion, :nombreObra, :responsableObra, :idComprobante, :uuidRelacionado, :serie, :folio, :subTotal, :total, :idOrigen, :longitud, :latitud, :pagina, :explorador, :sistemaOperativo, :ip)}", nativeQuery = true)	
	int insertarComp(@Param("rfc") String rfc
			, @Param("ticket") String ticket
			, @Param("razonSocial") String razonSocial
			, @Param("usoCfdi") String usoCfdi
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
			, @Param("longitud") String longitud
			, @Param("latitud") String latitud
			, @Param("pagina") String pagina
			, @Param("explorador") String explorador
			, @Param("sistemaOperativo") String sistemaOperativo
			, @Param("ip") String ip
			);

	@Query(value = "{call uspExistComp (:rfc, :ticket)}", nativeQuery = true)	
	int existComp(@Param("rfc") String rfc, @Param("ticket") String ticket);

	@Query(value = "{call uspInsertarLogFactura (:rfc, :ticket, :email, :pac, :idFacturaPac, :idEstatusFactura)}", nativeQuery = true)	
	int insertarLogFacturacion(@Param("rfc") String rfc
			, @Param("ticket") String ticket
			, @Param("email") String email
			, @Param("pac") int pac
			, @Param("idFacturaPac") int idFacturaPac
			, @Param("idEstatusFactura") int idEstatusFactura
			);
	
	ComplementosEntity findByUuid(String uuid);	
	

}

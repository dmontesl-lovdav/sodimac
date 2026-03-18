package com.sodimac.facturacion.repository.fac;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.sodimac.facturacion.entity.fac.ClientesTemporalEntity;

@Repository("clientesTemporalRepository")
public interface ClientesTemporalRepository extends JpaRepository<ClientesTemporalEntity, Integer> {
	
	ClientesTemporalEntity findTop1ByRfcOrderByFechaCreacionDesc(String rfc);

	@Query(value = "{call uspCrearTemporal (:rfc, :ticket, :razonSocial, :usoCfdi, :email, :autorizoGuardado, :nombreObra, :responsableObra, :regimenFiscal, :codigoPostal)}", nativeQuery = true)	
	int insertarTemporal(@Param("rfc") String rfc
			, @Param("ticket") String ticket
			, @Param("razonSocial") String razonSocial
			, @Param("usoCfdi") Integer usoCfdi
			, @Param("email") String email
			, @Param("autorizoGuardado") int autorizoGuardado
			, @Param("nombreObra") String nombreObra
			, @Param("responsableObra") String responsableObra
			, @Param("regimenFiscal") String regimenFiscal
			, @Param("codigoPostal") String codigoPostal
			);
	
}

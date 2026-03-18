package com.sodimac.facturacion.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.sodimac.facturacion.entity.LogErroresEntity;

@Repository("logErroresRepository")
public interface LogErroresRepository extends JpaRepository<LogErroresEntity, Integer> {

	@Modifying
	@Query(value = "{call uspInsertaLogErrores (:errorLog, :objeto, :params, :idUsuario, :longitud, :latitud, :pagina, :explorador, :sistemaOperativo, :ip)}", nativeQuery = true)	
	int registrarError(@Param("errorLog") String errorLog
			, @Param("objeto") String objeto
			, @Param("params") String params
			, @Param("idUsuario") int idUsuario
			, @Param("longitud") String longitud
			, @Param("latitud") String latitud
			, @Param("pagina") String pagina
			, @Param("explorador") String explorador
			, @Param("sistemaOperativo") String sistemaOperativo
			, @Param("ip") String ip
			);

	@Modifying
	@Query(value = "{call uspInsertaLogErrores (:errorLog, :objeto, :params, :idUsuario, :longitud, :latitud, :pagina, :explorador, :sistemaOperativo, :ip, :ticket, :rfc, :xml, :idFacturaPac, :sessionId)}", nativeQuery = true)	
	int registrarErrorV2(@Param("errorLog") String errorLog
			, @Param("objeto") String objeto
			, @Param("params") String params
			, @Param("idUsuario") int idUsuario
			, @Param("longitud") String longitud
			, @Param("latitud") String latitud
			, @Param("pagina") String pagina
			, @Param("explorador") String explorador
			, @Param("sistemaOperativo") String sistemaOperativo
			, @Param("ip") String ip
			, @Param("ticket") String ticket
			, @Param("rfc") String rfc
			, @Param("xml") String xml
			, @Param("idFacturaPac") int idFacturaPac
			, @Param("sessionId") String sessionId
			);
	
}
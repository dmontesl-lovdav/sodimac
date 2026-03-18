package com.sodimac.facturacion.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.sodimac.facturacion.entity.CatActividadesEntity;

@Repository("catActividadesRepository")
public interface CatActividadesRepository extends JpaRepository<CatActividadesEntity, Integer> {

	@Modifying
	@Query(value = "{call uspInsertaBitacoraActividades (:idActividad, :descripcion, :ticket, :idUsuario, :longitud, :latitud, :pagina, :explorador, :sistemaOperativo, :ip, :rfc, :sessionId)}", nativeQuery = true)	
	int registrarActividad(@Param("idActividad") int idActividad
			, @Param("descripcion") String descripcion
			, @Param("ticket") String ticket
			, @Param("idUsuario") int idUsuario
			, @Param("longitud") String longitud
			, @Param("latitud") String latitud
			, @Param("pagina") String pagina
			, @Param("explorador") String explorador
			, @Param("sistemaOperativo") String sistemaOperativo
			, @Param("ip") String ip
			, @Param("rfc") String rfc
			, @Param("sessionId") String sessionId
			);

}

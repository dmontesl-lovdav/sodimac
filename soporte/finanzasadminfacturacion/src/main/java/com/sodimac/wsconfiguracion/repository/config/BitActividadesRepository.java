package com.sodimac.wsconfiguracion.repository.config;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.sodimac.wsconfiguracion.entity.config.BitActividadesEntity;

@Repository("bitActividadesRepositoryConfig")
public interface BitActividadesRepository extends JpaRepository<BitActividadesEntity, Integer> {

	@Query(value = "{call uspInsertaBitacoraActividades (:idActividad, :descripcion, :ticket, :longitud, :latitud, :pagina, :explorador, :sistemaOperativo, :ip, :sessionId)}", nativeQuery = true)	
	int registrarActividad(@Param("idActividad") int idActividad
			, @Param("descripcion") String descripcion
			, @Param("ticket") String ticket
			, @Param("longitud") String longitud
			, @Param("latitud") String latitud
			, @Param("pagina") String pagina
			, @Param("explorador") String explorador
			, @Param("sistemaOperativo") String sistemaOperativo
			, @Param("ip") String ip
			, @Param("sessionId") String sessionId
			);
	
}

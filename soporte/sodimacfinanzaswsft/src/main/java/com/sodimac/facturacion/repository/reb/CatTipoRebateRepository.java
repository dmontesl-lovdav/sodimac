package com.sodimac.facturacion.repository.reb;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.sodimac.facturacion.entity.reb.CatTipoRebateEntity;

@Repository("catTipoRebateRepository")
public interface CatTipoRebateRepository extends JpaRepository<CatTipoRebateEntity, Integer> {

	@Query(value = "execute usp_ObtieneContabilizados", nativeQuery = true)	
	List<Object[]> getDescuentos();

	@Query(value = "execute usp_ActualizaTimbrado :numeroDocumento, :numeroReferencia, :ticket, :uuid, :fechaTimbrado", nativeQuery = true)
	int actualizaTimbrado(@Param("numeroDocumento") String numeroDocumento
			, @Param("numeroReferencia") String numeroReferencia
			, @Param("ticket") String ticket
			, @Param("uuid") String uuid
			, @Param("fechaTimbrado") String fechaTimbrado);
	
}

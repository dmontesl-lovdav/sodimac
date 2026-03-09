package com.sodimac.cfdi.repository.fiscal.documento;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import com.sodimac.cfdi.entity.fiscal.CatTipoDocumentoEntity;

@Repository
public interface CatTipoDocumentoRepository extends JpaRepository<CatTipoDocumentoEntity, Integer>  {

	@Query(
		    " select m  " +
			" from CatTipoDocumentoEntity m " +
			" where m.estatus = 1" +
		    " ORDER BY m.nombre"
			)
	public List<CatTipoDocumentoEntity> findTiposDocumento();
}

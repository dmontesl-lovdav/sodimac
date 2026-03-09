package com.sodimac.cfdi.repository.fiscal.documento;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.sodimac.cfdi.entity.fiscal.CatDocumentoConfEntity;

@Repository
public interface CatDocumentoConfRepository extends JpaRepository<CatDocumentoConfEntity, Integer>  {

	public CatDocumentoConfEntity findByIdDocumentoConf(Integer idDocumentoConf);
	
}

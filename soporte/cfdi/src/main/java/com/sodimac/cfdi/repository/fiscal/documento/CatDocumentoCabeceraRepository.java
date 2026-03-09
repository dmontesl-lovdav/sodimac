package com.sodimac.cfdi.repository.fiscal.documento;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.sodimac.cfdi.entity.fiscal.CatDocumentoCabeceraEntity;

@Repository
public interface CatDocumentoCabeceraRepository extends JpaRepository<CatDocumentoCabeceraEntity, Integer>  {

	public CatDocumentoCabeceraEntity findByIdDocumentoCabecera(Integer idDocumentoCabecera);
	
}

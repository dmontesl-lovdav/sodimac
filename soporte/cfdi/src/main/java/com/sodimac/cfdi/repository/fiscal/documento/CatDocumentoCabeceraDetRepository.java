package com.sodimac.cfdi.repository.fiscal.documento;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.sodimac.cfdi.entity.fiscal.CatDocumentoCabeceraDetEntity;

@Repository
public interface CatDocumentoCabeceraDetRepository extends JpaRepository<CatDocumentoCabeceraDetEntity, Integer>  {

	public List<CatDocumentoCabeceraDetEntity> findByIdDocumentoCabeceraAndEstatus(Integer idDocumentoCabecera, Integer estatus);
}

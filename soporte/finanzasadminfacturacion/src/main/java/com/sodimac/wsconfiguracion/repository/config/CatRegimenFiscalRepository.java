package com.sodimac.wsconfiguracion.repository.config;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.sodimac.wsconfiguracion.entity.config.CatRegimenFiscalEntity;
import com.sodimac.wsconfiguracion.entity.config.CatTipoPersonaEntity;

@Repository("catRegimenFiscalRepositoryConfig")
public interface CatRegimenFiscalRepository extends JpaRepository<CatRegimenFiscalEntity, Integer> {

	public List<CatRegimenFiscalEntity> findByCatTipoPersonaEntityAndActivo(CatTipoPersonaEntity catTipoPersonaEntity, Boolean activo);
	
	public List<CatRegimenFiscalEntity> findByCatTipoPersonaEntityInAndActivo(List<CatTipoPersonaEntity> catTipoPersonaEntity, Boolean activo);
}

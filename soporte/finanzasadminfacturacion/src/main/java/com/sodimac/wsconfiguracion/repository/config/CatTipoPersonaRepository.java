package com.sodimac.wsconfiguracion.repository.config;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.sodimac.wsconfiguracion.entity.config.CatTipoPersonaEntity;

@Repository("catTipoPersonaRepositoryConfig")
public interface CatTipoPersonaRepository extends JpaRepository<CatTipoPersonaEntity, Integer> {

	public List<CatTipoPersonaEntity> findByIdIn(List<Integer> ids);
	
}

package com.sodimac.wsconfiguracion.repository.config;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.sodimac.wsconfiguracion.entity.config.CatMesesEntity;

@Repository("catMesesRepositoryConfig")
public interface CatMesesRepository extends JpaRepository<CatMesesEntity, Integer>{

	CatMesesEntity findByClave(String clave);
	
}

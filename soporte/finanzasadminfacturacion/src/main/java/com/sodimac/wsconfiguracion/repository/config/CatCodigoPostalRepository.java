package com.sodimac.wsconfiguracion.repository.config;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.sodimac.wsconfiguracion.entity.config.CatCodigoPostalEntity;

@Repository("catCodigoPostalRepositoryConfig")
public interface CatCodigoPostalRepository extends JpaRepository<CatCodigoPostalEntity, Integer> {
	
}

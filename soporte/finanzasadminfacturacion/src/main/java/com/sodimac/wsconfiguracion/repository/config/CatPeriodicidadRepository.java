package com.sodimac.wsconfiguracion.repository.config;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.sodimac.wsconfiguracion.entity.config.CatPeriodicidadEntity;

@Repository("catPeriodicidadRepositoryConfig")
public interface CatPeriodicidadRepository extends JpaRepository<CatPeriodicidadEntity, Integer>{

	CatPeriodicidadEntity findByClave(String clave);
	
}

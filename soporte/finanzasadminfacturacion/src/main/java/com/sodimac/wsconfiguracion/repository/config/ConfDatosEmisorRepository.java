package com.sodimac.wsconfiguracion.repository.config;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.sodimac.wsconfiguracion.entity.config.ConfDatosEmisorEntity;

@Repository("confDatosEmisorRepositoryConfig")
public interface ConfDatosEmisorRepository extends JpaRepository<ConfDatosEmisorEntity, Integer> {

	ConfDatosEmisorEntity findByRfc(String rfc);
	
	
}

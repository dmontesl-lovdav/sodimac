package com.sodimac.wsconfiguracion.repository.config;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import com.sodimac.wsconfiguracion.entity.config.PacEntity;

@Repository("pacRepositoryConfig")
public interface PacRepository extends JpaRepository<PacEntity, Integer> {

	@Query(value = "{call uspObtenerPacDefault ()}", nativeQuery = true)	
	int getIdDefault();
}

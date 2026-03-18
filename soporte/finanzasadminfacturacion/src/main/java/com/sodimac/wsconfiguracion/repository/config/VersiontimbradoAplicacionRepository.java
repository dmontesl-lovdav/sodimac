package com.sodimac.wsconfiguracion.repository.config;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.sodimac.wsconfiguracion.entity.config.VersiontimbradoAplicacionEntity;

@Repository("versiontimbradoAplicacionRepositoryConfig")
public interface VersiontimbradoAplicacionRepository extends JpaRepository<VersiontimbradoAplicacionEntity, Integer> {
	
	public VersiontimbradoAplicacionEntity findByIdcataplicacionesAndActivo(int idAplicacion, Boolean activo);

}

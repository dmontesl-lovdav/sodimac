package com.sodimac.wsconfiguracion.repository.config;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.sodimac.wsconfiguracion.entity.config.CatConfiguracionEntity;


@Repository("catConfiguracionRepositoryConfig")
public interface CatConfiguracionRepository extends JpaRepository<CatConfiguracionEntity, Integer> {

    @Query(value = "select valor from catconfiguracion where NombreCampo = :NombreCampo", nativeQuery = true) 
    String findParameterByKey(@Param("NombreCampo") String NombreCampo);
	
}

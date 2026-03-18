package com.sodimac.facturacion.repository.fis;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.sodimac.facturacion.entity.fis.CatConfiguracionEntity;

@Repository("catConfiguracionFisRepository")
public interface CatConfiguracionRepository extends JpaRepository<CatConfiguracionEntity, String> {

    @Query(value = "select valor from catconfiguracion where NombreCampo = :NombreCampo", nativeQuery = true) 
    String findParameterByKey(@Param("NombreCampo") String NombreCampo);
	
}

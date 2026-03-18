package com.sodimac.facturacion.repository.ws;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.sodimac.facturacion.entity.ws.ConfiguracionWsEntity;

@Repository("configuracionWsRepository")
public interface ConfiguracionWsRepository extends JpaRepository<ConfiguracionWsEntity, Integer> {

    @Query(value = "select valor from catConfiguracion where NombreCampo = :NombreCampo", nativeQuery = true) 
    String findParameterByKey(@Param("NombreCampo") String NombreCampo);
	
}

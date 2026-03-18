package com.sodimac.facturacion.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.sodimac.facturacion.entity.ConfiguracionTokenEntity;

@Repository("tokenRepository")
public interface TokenRepository extends JpaRepository<ConfiguracionTokenEntity, Integer> {

	@Query(value = "{call uspGetTokenMultiple (:sessionId, :rfc, :email)}", nativeQuery = true)	
	String generarToken(@Param("sessionId") String sessionId, @Param("rfc") String rfc, @Param("email") String email);	
	
	@Query(value = "{call uspExistToken (:sessionId, :token, :rfc, :email)}", nativeQuery = true)	
	int existToken(@Param("sessionId") String sessionId, @Param("token") String token , @Param("rfc") String rfc, @Param("email") String email);
	
	ConfiguracionTokenEntity findFirstByOrderByIdConfTokenDesc();
}
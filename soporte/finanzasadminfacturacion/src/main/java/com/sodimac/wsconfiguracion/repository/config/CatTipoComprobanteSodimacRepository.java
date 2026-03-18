package com.sodimac.wsconfiguracion.repository.config;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.sodimac.wsconfiguracion.entity.config.CatTipoComprobanteSodimacEntity;

@Repository("catTipoComprobanteSodimacRepositoryConfig")
public interface CatTipoComprobanteSodimacRepository extends JpaRepository<CatTipoComprobanteSodimacEntity, Integer> {

	public CatTipoComprobanteSodimacEntity findByTipocomprobante(String tipocomprobante);
	
}

package com.sodimac.cfdi.repository.fiscal;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import com.sodimac.cfdi.entity.fiscal.RelacionVentaFacturacionEntity;

@Repository
public interface RelacionVentaFacturacionRepository extends JpaRepository<RelacionVentaFacturacionEntity, String> {

	@Query("SELECT u FROM RelacionVentaFacturacionEntity u")
	public List<RelacionVentaFacturacionEntity> findRelacionVentaFacturacionCfdi();

}

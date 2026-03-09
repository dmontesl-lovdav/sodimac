package com.sodimac.cfdi.repository.fiscal.catalogospdf;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.sodimac.cfdi.entity.fiscal.catalogospdf.CatTipoDeComprobanteEntity;

@Repository("catTipoDeComprobanteRepository")
public interface CatTipoDeComprobanteRepository extends JpaRepository<CatTipoDeComprobanteEntity, Integer> {
	CatTipoDeComprobanteEntity findByIdComprobante(String idComprobante);
}

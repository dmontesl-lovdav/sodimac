package com.sodimac.facturacion.repository.fac.catalogospdf;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.sodimac.facturacion.entity.fac.catalogospdf.CatTipoDeComprobanteEntity;

@Repository("catTipoDeComprobanteRepository")
public interface CatTipoDeComprobanteRepository extends JpaRepository<CatTipoDeComprobanteEntity, Integer> {
	CatTipoDeComprobanteEntity findByIdComprobante(String idComprobante);
}

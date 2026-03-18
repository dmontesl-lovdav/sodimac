package com.sodimac.facturacion.repository.fac.catalogospdf;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.sodimac.facturacion.entity.fac.catalogospdf.CatRegimenFiscalEntity;

@Repository("catRegimenFiscalRepository")
public interface CatRegimenFiscalRepository extends JpaRepository<CatRegimenFiscalEntity, Integer> {

}

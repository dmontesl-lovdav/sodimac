package com.sodimac.cfdi.repository.fiscal.catalogospdf;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.sodimac.cfdi.entity.fiscal.catalogospdf.CatMonedaEntity;

@Repository("catMonedaRepository")
public interface CatMonedaRepository extends JpaRepository<CatMonedaEntity, String> {

}

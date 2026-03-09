package com.sodimac.cfdi.repository.fiscal.catalogospdf;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.sodimac.cfdi.entity.fiscal.catalogospdf.CatMetodoPagoEntity;

@Repository("CatMetodoPagoRepository")
public interface CatMetodoPagoRepository extends JpaRepository<CatMetodoPagoEntity, String> {

}

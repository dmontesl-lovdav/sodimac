package com.sodimac.facturacion.repository.fac;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.sodimac.facturacion.entity.fac.CatMensajesEntity;

@Repository("catMensajesRepository")
public interface CatMensajesRepository extends JpaRepository<CatMensajesEntity, Integer> {

}

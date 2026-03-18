package com.sodimac.facturacion.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.sodimac.facturacion.entity.CatMensajesEntity;

@Repository("catMensajesRepository")
public interface CatMensajesRepository extends JpaRepository<CatMensajesEntity, Integer> {

}

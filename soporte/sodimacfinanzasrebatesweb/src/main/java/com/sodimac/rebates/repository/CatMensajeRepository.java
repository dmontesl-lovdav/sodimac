package com.sodimac.rebates.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.sodimac.rebates.model.entity.CatMensajeEntity;

@Repository
public interface CatMensajeRepository extends JpaRepository<CatMensajeEntity, Integer> {

	public CatMensajeEntity findByClaveAndActivo(String clave, boolean activo);
}

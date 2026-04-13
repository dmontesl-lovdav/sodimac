package com.sodimac.rebates.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.sodimac.rebates.model.CatTiendaEntity;

@Repository
public interface CatTiendaRepository extends JpaRepository<CatTiendaEntity, Integer> {

	public List<CatTiendaEntity> findByActivo(boolean activo);

}

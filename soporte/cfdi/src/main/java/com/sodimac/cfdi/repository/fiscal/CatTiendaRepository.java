package com.sodimac.cfdi.repository.fiscal;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.sodimac.cfdi.entity.fiscal.CatTiendaEntity;

@Repository
public interface CatTiendaRepository extends JpaRepository<CatTiendaEntity, Integer> {

	public List<CatTiendaEntity> findByActivo(boolean activo);

}
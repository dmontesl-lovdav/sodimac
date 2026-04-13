package com.sodimac.rebates.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.sodimac.rebates.model.entity.CatEventoEntity;
import com.sodimac.rebates.model.entity.CatFlujoEstatusEntity;
import com.sodimac.rebates.model.entity.CatRolEntity;

@Repository
public interface CatFlujoEstatusRepository extends JpaRepository<CatFlujoEstatusEntity, Integer> {

	public List<CatFlujoEstatusEntity> findByRolInAndEventoAndEstatusOrigenAndActivo(List<CatRolEntity> roles
			, CatEventoEntity evento, Integer estatusOrigen, boolean activo);
	
	public List<CatFlujoEstatusEntity> findByEventoAndEstatusOrigenAndActivo(CatEventoEntity evento, 
			Integer estatusOrigen, boolean activo);
	
}

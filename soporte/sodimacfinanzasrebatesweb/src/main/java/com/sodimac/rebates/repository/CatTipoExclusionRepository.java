package com.sodimac.rebates.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.sodimac.rebates.model.entity.CatTipoExclusionEntity;

@Repository
public interface CatTipoExclusionRepository extends JpaRepository<CatTipoExclusionEntity, Integer> {

	public List<CatTipoExclusionEntity> findByActivo(boolean activo);

}

package com.sodimac.rebates.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.sodimac.rebates.model.entity.CatEstatusExclusionEntity;

@Repository
public interface CatEstatusExclusionRepository extends JpaRepository<CatEstatusExclusionEntity, Integer> {

	public List<CatEstatusExclusionEntity> findByActivo(boolean activo);

}

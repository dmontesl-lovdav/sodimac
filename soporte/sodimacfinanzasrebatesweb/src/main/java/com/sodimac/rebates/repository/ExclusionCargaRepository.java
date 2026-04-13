package com.sodimac.rebates.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.sodimac.rebates.model.entity.ExclusionCargaEntity;

@Repository
public interface ExclusionCargaRepository extends JpaRepository<ExclusionCargaEntity, Long> {

	public List<ExclusionCargaEntity> findByIdExclusionAndActivo(Integer idExclusion, boolean activo);
	
	public ExclusionCargaEntity findByIdExclusion(Integer idExclusion);
	
	public ExclusionCargaEntity findByIdExclusionCarga(Long idExclusionCarga);
}

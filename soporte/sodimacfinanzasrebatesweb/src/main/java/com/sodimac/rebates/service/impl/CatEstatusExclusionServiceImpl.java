package com.sodimac.rebates.service.impl;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.sodimac.rebates.dto.CatEstatusExclusionDto;
import com.sodimac.rebates.enums.EEstatus;
import com.sodimac.rebates.mapper.CatEstatusExclusionMapper;
import com.sodimac.rebates.model.entity.CatEstatusExclusionEntity;
import com.sodimac.rebates.repository.CatEstatusExclusionRepository;
import com.sodimac.rebates.service.ICatEstatusExclusionService;

@Service
public class CatEstatusExclusionServiceImpl implements ICatEstatusExclusionService {

	@Autowired
	private CatEstatusExclusionRepository catEstatusExclusionRepository;
	
	@Override
	public List<CatEstatusExclusionDto> getCatEstatusExclusion() {
		List<CatEstatusExclusionDto> listDtos = null;
		List<CatEstatusExclusionEntity> listEntities = this.catEstatusExclusionRepository.findByActivo(EEstatus.ACTIVO.isActivo());
		if (listEntities != null) {
			listDtos = CatEstatusExclusionMapper.convertDtos(listEntities);
		}
		return listDtos;
	}

}

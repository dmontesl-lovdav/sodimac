package com.sodimac.rebates.mapper;

import java.util.ArrayList;
import java.util.List;

import com.sodimac.rebates.dto.CatEstatusExclusionDto;
import com.sodimac.rebates.model.entity.CatEstatusExclusionEntity;

public final class CatEstatusExclusionMapper {

	public static CatEstatusExclusionDto convertDto(CatEstatusExclusionEntity entity) {
		CatEstatusExclusionDto dto = new CatEstatusExclusionDto();
		dto.setIdCatEstatusExclusion( entity.getIdCatEstatusExclusion() );
		dto.setDescripcion( entity.getDescripcion() );
		dto.setClave( entity.getClave() );
		dto.setActivo( entity.isActivo() );
		return dto;
	}

	public static CatEstatusExclusionEntity convertEntity(CatEstatusExclusionDto dto) {
		CatEstatusExclusionEntity entity = new CatEstatusExclusionEntity();
		entity.setIdCatEstatusExclusion(dto.getIdCatEstatusExclusion());
		return entity;
	}

	public static List<CatEstatusExclusionDto> convertDtos(List<CatEstatusExclusionEntity> listEntities) {
		List<CatEstatusExclusionDto> listDtos = new ArrayList<>();
		if (listEntities != null) {
			for (CatEstatusExclusionEntity entity : listEntities) {
				listDtos.add( CatEstatusExclusionMapper.convertDto(entity) );
			}
		}
		return listDtos;
	}
	
}

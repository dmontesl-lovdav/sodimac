package com.sodimac.rebates.mapper;

import java.util.ArrayList;
import java.util.List;

import com.sodimac.rebates.dto.RelPeriodoTipoRebateDto;
import com.sodimac.rebates.model.RelPeriodoTipoRebate;

public final class RelPeriodoTipoRebateMapper {

	private RelPeriodoTipoRebateMapper() {
		super();
	}
	
	public static RelPeriodoTipoRebateDto convertToDto(RelPeriodoTipoRebate entity) {
		RelPeriodoTipoRebateDto dto = new RelPeriodoTipoRebateDto();
		dto.setId( entity.getId() );
		dto.setActivo(entity.isActivo());
		
		dto.setCatTipoRebate(  CatTipoRebateMapper.convertDto(entity.getCatTipoRebate()) );
		dto.setPeriodo( PeriodoMapper.convertDtoComplejo(entity.getPeriodo()) );
		return dto;
	}
	
	public static RelPeriodoTipoRebateDto convertToDtoSimple(RelPeriodoTipoRebate entity) {
		RelPeriodoTipoRebateDto dto = new RelPeriodoTipoRebateDto();
		dto.setId( entity.getId() );
		dto.setActivo(entity.isActivo());
		
		dto.setCatTipoRebate(  CatTipoRebateMapper.convertDto(entity.getCatTipoRebate()) );
		dto.setPeriodo( PeriodoMapper.convertToDtoSimple(entity.getPeriodo()) );
		return dto;
	}
	
	private static RelPeriodoTipoRebate convertToEntity(RelPeriodoTipoRebateDto dto) {
		RelPeriodoTipoRebate entity = new RelPeriodoTipoRebate();
		entity.setId( dto.getId() );
		entity.setActivo(dto.isActivo());
		
		entity.setCatTipoRebate(  CatTipoRebateMapper.convertToEntity(dto.getCatTipoRebate()) );
		entity.setPeriodo( PeriodoMapper.convertToEntity(dto.getPeriodo()) );
		return entity;
	}
	
	public static List<RelPeriodoTipoRebateDto> convertToDtos(List<RelPeriodoTipoRebate> relPeriodoTipoRebate) {
		List<RelPeriodoTipoRebateDto> dtos = new ArrayList<>();
		if (relPeriodoTipoRebate != null) {
			for (RelPeriodoTipoRebate entity : relPeriodoTipoRebate) {
				dtos.add( convertToDto(entity) );
			}
		}
		return dtos;
	}
	
	public static List<RelPeriodoTipoRebate> convertToEntities(List<RelPeriodoTipoRebateDto> dtos) {
		List<RelPeriodoTipoRebate> entities = new ArrayList<>();
		if (dtos != null) {
			for (RelPeriodoTipoRebateDto dto : dtos) {
				entities.add( convertToEntity(dto) );
			}
		}
		return entities;
	}

	public static List<RelPeriodoTipoRebateDto> convertToDtosSimple(List<RelPeriodoTipoRebate> entities) {
		List<RelPeriodoTipoRebateDto> dtos = new ArrayList<>();
		if (entities != null) {
			for (RelPeriodoTipoRebate entity : entities) {
				dtos.add( convertToDtoSimple(entity) );
			}
		}
		return dtos;
	}

	
}

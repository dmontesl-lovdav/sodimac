package com.sodimac.rebates.mapper;

import com.sodimac.rebates.model.CatEventoDto;
import com.sodimac.rebates.model.entity.CatEventoEntity;

public final class CatEventoMapper {

	private CatEventoMapper() {
		super();
	}
	
	public static CatEventoDto convertToDto(CatEventoEntity entity) {
		CatEventoDto dto = new CatEventoDto();
		dto.setIdCatEvento( entity.getIdCatEvento() );
		dto.setClave( entity.getClave() );
		dto.setDescripcion( entity.getDescripcion() );
		dto.setActivo( entity.getActivo() );
		return dto;
	}
	
	public static CatEventoEntity convertToEntity(CatEventoDto dto) {
		CatEventoEntity entity = new CatEventoEntity();
		entity.setIdCatEvento( dto.getIdCatEvento() );
		return entity;
	}
}

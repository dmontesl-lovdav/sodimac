package com.sodimac.rebates.mapper;

import java.util.ArrayList;
import java.util.List;

import com.sodimac.rebates.dto.CatFlujoEstatusDto;
import com.sodimac.rebates.model.entity.CatFlujoEstatusEntity;

public final class CatFlujoEstatusMapper {

	private CatFlujoEstatusMapper() {
		super();
	}
	
	public static CatFlujoEstatusDto convertToDto(CatFlujoEstatusEntity entity) {
		CatFlujoEstatusDto dto = new CatFlujoEstatusDto();
		dto.setIdCatFlujoEstatus( entity.getIdCatFlujoEstatus() );
		dto.setEstatusOrigen( entity.getEstatusOrigen() );
		dto.setEstatusDestino( entity.getEstatusDestino() );
		dto.setRol( CatRolMapper.convertToDto(entity.getRol()) );
		dto.setEvento( CatEventoMapper.convertToDto( entity.getEvento() ) );
		return dto;
	}

	public static List<CatFlujoEstatusDto> convertToDtos(List<CatFlujoEstatusEntity> entities) {
		List<CatFlujoEstatusDto> dtos = new ArrayList<>();
		if (entities != null) {
			for (CatFlujoEstatusEntity entity : entities) {
				dtos.add( convertToDto(entity) );
			}
		}
		return dtos;
	}
}

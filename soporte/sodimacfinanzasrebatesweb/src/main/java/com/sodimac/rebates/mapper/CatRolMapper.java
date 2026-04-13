package com.sodimac.rebates.mapper;

import java.util.ArrayList;
import java.util.List;

import com.sodimac.rebates.dto.CatRolDto;
import com.sodimac.rebates.model.entity.CatRolEntity;

public final class CatRolMapper {

	private CatRolMapper() {
		super();
	}
	
	public static CatRolDto convertToDto(CatRolEntity entity) {
		CatRolDto dto = new CatRolDto();
		dto.setId( entity.getId() );
		dto.setNombre( entity.getNombre() );
		return dto;
	}
	
	public static CatRolEntity convertToEntity(CatRolDto dto) {
		CatRolEntity entity = new CatRolEntity();
		entity.setId( dto.getId() );
		entity.setNombre( dto.getNombre() );
		return entity;
	}

	public static List<CatRolDto> convertToDtos(List<CatRolEntity> entities) {
		List<CatRolDto> dtos = new ArrayList<>();
		if (entities != null) {
			for (CatRolEntity entity : entities) {
				dtos.add( convertToDto(entity) );
			}
		}
		return dtos;
	}

	public static List<CatRolEntity> convertToEntities(List<CatRolDto> rolesDto) {
		List<CatRolEntity> entities = new ArrayList<>();
		if (rolesDto != null) {
			for (CatRolDto dto : rolesDto) {
				entities.add(convertToEntity(dto));
			}
		}
		return entities;
	}
}

package com.sodimac.rebates.mapper;

import java.util.ArrayList;
import java.util.List;

import com.sodimac.rebates.dto.CatTipoExclusionDto;
import com.sodimac.rebates.model.entity.CatTipoExclusionEntity;

public final class CatTipoExclusionMapper {
	
	private static final int INDEX_ID_CAT_TIPO_EXCLUSION = 0;
	private static final int INDEX_DESCRIPCION = 1;
	private static final int INDEX_CLAVE = 2;

	public static CatTipoExclusionDto convertDto(CatTipoExclusionEntity entity) {
		CatTipoExclusionDto dto = new CatTipoExclusionDto();
		dto.setIdCatTipoExclusion( entity.getIdCatTipoExclusion() );
		dto.setDescripcion( entity.getDescripcion() );
		dto.setClave( entity.getClave() );
		dto.setActivo( entity.isActivo() );
		return dto;
	}
	
	public static CatTipoExclusionDto convertDto(Object[] object) {
		CatTipoExclusionDto dto = new CatTipoExclusionDto();
		dto.setIdCatTipoExclusion( Integer.valueOf(object[INDEX_ID_CAT_TIPO_EXCLUSION].toString()) );
		dto.setDescripcion( object[INDEX_DESCRIPCION].toString() );
		dto.setClave( object[INDEX_CLAVE].toString() );
		dto.setActivo( true );
		return dto;
	}
	
	public static CatTipoExclusionEntity convertEntity(CatTipoExclusionDto dto) {
		CatTipoExclusionEntity entity = new CatTipoExclusionEntity();
		entity.setIdCatTipoExclusion( dto.getIdCatTipoExclusion() );
		entity.setDescripcion( dto.getDescripcion() );
		entity.setClave( dto.getClave() );
		entity.setActivo( dto.isActivo() );
		return entity;
	}

	public static List<CatTipoExclusionDto> convertDtos(List<CatTipoExclusionEntity> listEntities) {
		List<CatTipoExclusionDto> listDtos = new ArrayList<>();
		if (listEntities != null) {
			for (CatTipoExclusionEntity entity : listEntities) {
				listDtos.add( CatTipoExclusionMapper.convertDto(entity) );
			}
		}
		return listDtos;
	}
	
	public static List<CatTipoExclusionDto> convertObjectToDtos(List<Object[]> listEntities) {
		List<CatTipoExclusionDto> listDtos = new ArrayList<>();
		if (listEntities != null) {
			for (Object[] entity : listEntities) {
				listDtos.add( CatTipoExclusionMapper.convertDto(entity) );
			}
		}
		return listDtos;
	}
}

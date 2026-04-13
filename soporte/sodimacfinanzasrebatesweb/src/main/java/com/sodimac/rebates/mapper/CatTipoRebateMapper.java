package com.sodimac.rebates.mapper;

import java.util.ArrayList;
import java.util.List;

import com.sodimac.rebates.dto.CatTipoRebateDto;
import com.sodimac.rebates.model.TipoRebate;

public final class CatTipoRebateMapper {
	
	private static final int INDEX_ID_CAT_TIPO_REBATE = 0;
	private static final int INDEX_NOMENCLATURA = 1;
	private static final int INDEX_TIPO_REBATE = 2;

	public static CatTipoRebateDto convertDto(TipoRebate entity) {
		CatTipoRebateDto dto = new CatTipoRebateDto();
		dto.setIdCatTipoRebate( entity.getIdCatTipoRebate() );
		dto.setNomenclatura(entity.getNomenclatura());
		dto.setTipoRebate( entity.getTipoRebate() );
		dto.setActivo( entity.isActivo() );
		return dto;
	}
	
	public static TipoRebate convertToEntity(CatTipoRebateDto dto) {
		TipoRebate entity = new TipoRebate();
		entity.setIdCatTipoRebate( dto.getIdCatTipoRebate() );
		entity.setNomenclatura(dto.getNomenclatura());
		entity.setTipoRebate( dto.getTipoRebate() );
		entity.setActivo( dto.isActivo() );
		return entity;
	}
	
	public static CatTipoRebateDto convertDto(Object[] object) {
		CatTipoRebateDto dto = new CatTipoRebateDto();
		dto.setIdCatTipoRebate( Integer.valueOf(object[INDEX_ID_CAT_TIPO_REBATE].toString()) );
		dto.setNomenclatura( object[INDEX_NOMENCLATURA].toString() );
		dto.setTipoRebate( object[INDEX_TIPO_REBATE].toString() );
		dto.setActivo( true );
		return dto;
	}

	public static TipoRebate convertEntity(CatTipoRebateDto dto) {
		TipoRebate entity = new TipoRebate();
		entity.setIdCatTipoRebate(dto.getIdCatTipoRebate() );
		return entity;
	}

	public static List<CatTipoRebateDto> convertObjectToDtos(List<Object[]> listEntities) {
		List<CatTipoRebateDto> listDtos = new ArrayList<>();
		if (listEntities != null) {
			for (Object[] entity : listEntities) {
				listDtos.add( CatTipoRebateMapper.convertDto(entity) );
			}
		}
		return listDtos;
	}

	public static List<CatTipoRebateDto> convertDtos(List<TipoRebate> entities) {
		List<CatTipoRebateDto> dtos = new ArrayList<>();
		if (entities != null) {
			for (TipoRebate entity : entities) {
				dtos.add(convertDto(entity));
			}
		}
		return dtos;
	}	
}

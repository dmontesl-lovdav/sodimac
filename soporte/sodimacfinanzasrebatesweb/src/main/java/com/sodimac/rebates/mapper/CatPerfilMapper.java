package com.sodimac.rebates.mapper;

import java.util.ArrayList;
import java.util.List;

import com.sodimac.rebates.dto.CatPerfilDto;
import com.sodimac.rebates.model.entity.CatPerfilEntity;

public final class CatPerfilMapper {

	private CatPerfilMapper() {
		super();
	}
	
	public static CatPerfilDto convertToDto(CatPerfilEntity entity) {
		CatPerfilDto dto = new CatPerfilDto();
		dto.setId(entity.getId());
		dto.setNombre(entity.getNombre());
		dto.setActivo(entity.isActivo());
		dto.setUsuarioCreacion(entity.getUsuarioCreacion());
		dto.setFechaCreacion(entity.getFechaCreacion());
		dto.setFechaActualizacion(entity.getFechaActualizacion());
		return dto;
	}

	public static List<CatPerfilDto> convertToDtos(List<CatPerfilEntity> catPerfilList) {
		List<CatPerfilDto> listPerfiles = new ArrayList<>();
		if (catPerfilList != null) {
			for (CatPerfilEntity perfil : catPerfilList) {
				listPerfiles.add( convertToDto(perfil) );
			}
		}
		return listPerfiles;
	}
}

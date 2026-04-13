package com.sodimac.rebates.mapper;

import java.util.ArrayList;
import java.util.List;

import com.sodimac.rebates.dto.CatPerfilDto;
import com.sodimac.rebates.dto.CatUsuarioPerfilDto;
import com.sodimac.rebates.dto.UsuarioDto;
import com.sodimac.rebates.model.entity.CatUsuarioPerfilEntity;

public final class CatUsuarioPerfilMapper {

	private CatUsuarioPerfilMapper() {
		super();
	}
	
	public static CatUsuarioPerfilDto convertToDto(CatUsuarioPerfilEntity entity) {
		CatUsuarioPerfilDto dto = new CatUsuarioPerfilDto();
		CatPerfilDto perfil= CatPerfilMapper.convertToDto(entity.getPerfil() );
		UsuarioDto usuario = UsuarioMapper.convertDto( entity.getUsuario() );
		dto.setPerfil(perfil);
		dto.setUsuario(usuario);
		return dto;
	}

	public static List<CatUsuarioPerfilDto> convertToDtos(List<CatUsuarioPerfilEntity> listEntities) {
		List<CatUsuarioPerfilDto> dtos = new ArrayList<>();
		if (listEntities != null) {
			for (CatUsuarioPerfilEntity entity : listEntities) {
				dtos.add( convertToDto(entity) );
			}
		}
		return dtos;
	}
}

package com.sodimac.rebates.mapper;

import com.sodimac.rebates.dto.UsuarioDto;
import com.sodimac.rebates.model.Usuario;

public final class UsuarioMapper {

	public static UsuarioDto convertDto(Usuario entity) {
		StringBuilder sbNombre = new StringBuilder("");
		UsuarioDto dto = new UsuarioDto();
		if (entity != null) {
			dto.setId(entity.getId());
			dto.setNombre( entity.getNombre() );
			dto.setApellidoPaterno( entity.getApellidoPaterno() );
			dto.setApellidoMaterno( entity.getApellidoMaterno() );
			dto.setUsuario( entity.getUsuario() );
			
			if (entity.getNombre() != null) {
				sbNombre.append( entity.getNombre() ).append(" ");
			}
			if (entity.getApellidoPaterno() != null) {
				sbNombre.append(entity.getApellidoPaterno()).append(" ");
			}
			if (entity.getApellidoMaterno() != null) {
				sbNombre.append(entity.getApellidoMaterno());
			}
		}
		dto.setNombreCompleto( sbNombre.toString() );
		return dto;
	}
	
	public static Usuario convertEntity(UsuarioDto dto) {
		Usuario entity = null;
		if (dto != null) {
			entity = new Usuario();
			entity.setId( dto.getId() );
		}
		return entity;
	}
}

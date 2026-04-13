package com.sodimac.rebates.service;

import java.util.List;

import com.sodimac.rebates.dto.CatUsuarioPerfilDto;
import com.sodimac.rebates.enums.EPerfil;

public interface ICatUsuarioPerfilService {

	public List<CatUsuarioPerfilDto> getUsuarioPerfiles(Integer idUsuario);

	public boolean containsPerfil(Integer idUsuario, EPerfil perfil);
	
}

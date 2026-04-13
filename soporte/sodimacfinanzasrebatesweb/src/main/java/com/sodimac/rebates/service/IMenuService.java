package com.sodimac.rebates.service;

import com.sodimac.rebates.dto.MenuByPerfilDto;
import com.sodimac.rebates.dto.MenuByUsuarioDto;

public interface IMenuService {

	public MenuByPerfilDto getMenuByPerfil(int perfil, String Action, String contextPath);
	
	public MenuByUsuarioDto getMenuByUser(int idusuario, String Action, String contextPath);
}

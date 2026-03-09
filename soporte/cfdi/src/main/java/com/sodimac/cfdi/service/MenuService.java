package com.sodimac.cfdi.service;

import java.util.List;

import com.sodimac.cfdi.model.menu.MenuByPerfil;
import com.sodimac.cfdi.model.menu.MenuByUsuario;

public interface MenuService {

	public MenuByPerfil getMenuByPerfil(int perfil, String Action, String contextPath);
	
	public MenuByUsuario getMenuByUser(int idusuario, String Action, String contextPath);
	

}

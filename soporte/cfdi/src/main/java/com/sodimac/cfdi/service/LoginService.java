package com.sodimac.cfdi.service;

import java.util.List;

import com.sodimac.cfdi.entity.fiscal.CatOpcionesEntity;
import com.sodimac.cfdi.entity.fiscal.UsuariosEntity;

public interface LoginService {
	public UsuariosEntity validarLogin(String usuario, String password);
	public List<CatOpcionesEntity> obtenerOpcionesRol(int idRol);
}

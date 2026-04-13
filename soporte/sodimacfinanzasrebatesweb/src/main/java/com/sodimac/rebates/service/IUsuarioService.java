package com.sodimac.rebates.service;

import com.sodimac.rebates.model.Usuario;

public interface IUsuarioService {

	Usuario getById(Integer id, String oldPassword);

	Usuario getUser(String usuario, String pass);

	Usuario getUserEmail(String usuario);

	boolean save(Usuario usuario);

}

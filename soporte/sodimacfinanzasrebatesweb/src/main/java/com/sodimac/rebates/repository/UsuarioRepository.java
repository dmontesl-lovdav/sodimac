package com.sodimac.rebates.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import com.sodimac.rebates.model.Usuario;

public interface UsuarioRepository extends JpaRepository<Usuario, Integer> {

	Usuario findByUsuarioAndPass(String usuario, String pass);

	Usuario findByUsuario(String usuario);

}

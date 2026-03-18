package com.sodimac.wsconfiguracion.repository.config.entitymanager;

import java.util.ArrayList;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.sodimac.wsconfiguracion.config.User;
import com.sodimac.wsconfiguracion.entity.config.UsuarioWsEntity;
import com.sodimac.wsconfiguracion.repository.config.UsuarioWsRepository;

@Component
public class UsersWsRepository {

	@Autowired
	private UsuarioWsRepository usuarioWsRepository;

	private static List<User> users = new ArrayList<>();

	public List<User> getUsers() {

        if (users == null || users.isEmpty()) {
        	List<UsuarioWsEntity> usuariosActivos = usuarioWsRepository.findByActivo(1);

        	if (usuariosActivos != null && !usuariosActivos.isEmpty()) {
        		for (UsuarioWsEntity usuario : usuariosActivos) {
        			users.add(new User(usuario.getUsuario(), usuario.getContrasena(), usuario.getRol()));
        		}
        	}
        }

        return users;
    }
}

package com.sodimac.rebates.service.impl;

import java.util.ArrayList;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.sodimac.rebates.dto.CatUsuarioPerfilDto;
import com.sodimac.rebates.enums.EPerfil;
import com.sodimac.rebates.mapper.CatUsuarioPerfilMapper;
import com.sodimac.rebates.model.Usuario;
import com.sodimac.rebates.model.entity.CatUsuarioPerfilEntity;
import com.sodimac.rebates.repository.CatUsuarioPerfilRepository;
import com.sodimac.rebates.service.ICatUsuarioPerfilService;

@Service
public class CatUsuarioPerfilServiceImpl implements ICatUsuarioPerfilService {
	
	@Autowired
	private CatUsuarioPerfilRepository catUsuarioPerfilRepository;

	@Override
	public List<CatUsuarioPerfilDto> getUsuarioPerfiles(Integer idUsuario) {
		List<CatUsuarioPerfilDto> dtos = new ArrayList<>(); 
		
		Usuario usuario = new Usuario();
		usuario.setId( idUsuario );
		List<CatUsuarioPerfilEntity> listEntities = this.catUsuarioPerfilRepository.findByUsuario(usuario);
		if (listEntities != null) {
			dtos = CatUsuarioPerfilMapper.convertToDtos(listEntities);
		}
		return dtos;
	}
	
	@Override
	public boolean containsPerfil(Integer idUsuario, EPerfil perfil) {
		List<CatUsuarioPerfilDto> list = this.getUsuarioPerfiles(idUsuario);
		if (list != null) {
			for (CatUsuarioPerfilDto usuarioPerfil : list) {
				if (usuarioPerfil.getPerfil().getId() == perfil.getId()) {
					return true;
				}
			}
		}
		return false;
	}

}

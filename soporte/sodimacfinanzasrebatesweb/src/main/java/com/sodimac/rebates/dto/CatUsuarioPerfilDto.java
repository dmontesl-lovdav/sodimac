package com.sodimac.rebates.dto;

public class CatUsuarioPerfilDto {

	private UsuarioDto usuario;
	private CatPerfilDto perfil;	
	
	public UsuarioDto getUsuario() {
		return usuario;
	}

	public void setUsuario(UsuarioDto usuario) {
		this.usuario = usuario;
	}

	public CatPerfilDto getPerfil() {
		return perfil;
	}

	public void setPerfil(CatPerfilDto perfil) {
		this.perfil = perfil;
	}
}

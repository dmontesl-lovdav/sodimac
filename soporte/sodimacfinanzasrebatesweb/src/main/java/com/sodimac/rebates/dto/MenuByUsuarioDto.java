package com.sodimac.rebates.dto;

import java.util.ArrayList;
import java.util.List;

public class MenuByUsuarioDto {

	private int idUsuario;
	private List<String> paginas = new ArrayList<String>();
	private List<MenuItemDto> lstMenuItem = new ArrayList<MenuItemDto>();
	private String perfiles;

	public int getIdUsuario() {
		return idUsuario;
	}

	public void setIdUsuario(int idUsuario) {
		this.idUsuario = idUsuario;
	}

	public List<String> getPaginas() {
		return paginas;
	}

	public void setPaginas(List<String> paginas) {
		this.paginas = paginas;
	}

	public List<MenuItemDto> getLstMenuItem() {
		return lstMenuItem;
	}

	public void setLstMenuItem(List<MenuItemDto> lstMenuItem) {
		this.lstMenuItem = lstMenuItem;
	}

	public String getPerfiles() {
		return perfiles;
	}

	public void setPerfiles(String perfiles) {
		this.perfiles = perfiles;
	}

}

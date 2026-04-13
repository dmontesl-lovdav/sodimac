package com.sodimac.rebates.dto;

import java.util.ArrayList;
import java.util.List;

public class MenuByPerfilDto {
	private int perfil;
	private List<String> paginas = new ArrayList<String>();
	private List<MenuItemDto> lstMenuItem = new ArrayList<MenuItemDto>();
	
	public int getPerfil() {
		return perfil;
	}
	public void setPerfil(int perfil) {
		this.perfil = perfil;
	}
	public List<MenuItemDto> getLstMenuItem() {
		return lstMenuItem;
	}
	public void setLstMenuItem(List<MenuItemDto> lstMenuItem) {
		this.lstMenuItem = lstMenuItem;
	}
	public List<String> getPaginas() {
		return paginas;
	}
	public void setPaginas(List<String> paginas) {
		this.paginas = paginas;
	}
}

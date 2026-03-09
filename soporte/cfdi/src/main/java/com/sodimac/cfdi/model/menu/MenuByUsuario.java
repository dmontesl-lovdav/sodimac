package com.sodimac.cfdi.model.menu;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;

public class MenuByUsuario {

	private int idUsuario;
	private List<String> paginas = new ArrayList<String>();
	private List<MenuItem> lstMenuItem = new ArrayList<MenuItem>();
	private String perfiles;
	
	public int getIdUsuario() {
		return idUsuario;
	}
	public void setIdUsuario(int idUsuario) {
		this.idUsuario = idUsuario;
	}
	public List<MenuItem> getLstMenuItem() {
		return lstMenuItem;
	}
	public void setLstMenuItem(List<MenuItem> lstMenuItem) {
		this.lstMenuItem = lstMenuItem;
	}
	public List<String> getPaginas() {
		return paginas;
	}
	public void setPaginas(List<String> paginas) {
		this.paginas = paginas;
	}
	public String getPerfiles() {
		return perfiles;
	}
	public void setPerfiles(String perfiles) {
		this.perfiles = perfiles;
	}

	
	
	
}

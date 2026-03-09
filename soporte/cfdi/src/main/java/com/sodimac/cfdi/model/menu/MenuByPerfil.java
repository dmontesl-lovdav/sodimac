package com.sodimac.cfdi.model.menu;

import java.util.ArrayList;
import java.util.List;

public class MenuByPerfil {


	private int perfil;
	private List<String> paginas = new ArrayList<String>();
	private List<MenuItem> lstMenuItem = new ArrayList<MenuItem>();
	
	public int getPerfil() {
		return perfil;
	}
	public void setPerfil(int perfil) {
		this.perfil = perfil;
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

}

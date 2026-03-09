package com.sodimac.cfdi.model.menu;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import com.sodimac.cfdi.util.enums.EMenuType;

public class MenuItem implements Serializable {
	
	/**
	 * 
	 */
	private static final long serialVersionUID = -654477967500211025L;
	
	private int id;
	private String name;
	private String href;
	private String icon;
	private EMenuType eMenuType;
	private List<MenuItem> lstMenuItem = new ArrayList<MenuItem>();
	
	public MenuItem() {};
	
	public MenuItem(String name, EMenuType eMenuType, List<MenuItem> lstMenuItem, String href, int id, String icon) {
		this.name = name;
		this.eMenuType = eMenuType;
		this.lstMenuItem = lstMenuItem;
		this.href = href;
		this.id = id;
		this.icon = icon;
		
	};
	
	public String getName() {
		return name;
	}
	public void setName(String name) {
		this.name = name;
	}
	public String getHref() {
		return href;
	}
	public void setHref(String href) {
		this.href = href;
	}
	public EMenuType geteMenuType() {
		return eMenuType;
	}
	public void seteMenuType(EMenuType eMenuType) {
		this.eMenuType = eMenuType;
	}
	public List<MenuItem> getLstMenuItem() {
		return lstMenuItem;
	}
	public void setLstMenuItem(List<MenuItem> lstMenuItem) {
		this.lstMenuItem = lstMenuItem;
	}

	public int getId() {
		return id;
	}

	public void setId(int id) {
		this.id = id;
	}

	public String getIcon() {
		return icon;
	}

	public void setIcon(String icon) {
		this.icon = icon;
	}

}

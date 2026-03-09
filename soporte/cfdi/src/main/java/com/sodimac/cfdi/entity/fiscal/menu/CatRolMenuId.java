package com.sodimac.cfdi.entity.fiscal.menu;

import java.io.Serializable;

import javax.persistence.Column;
import javax.persistence.Embeddable;


@Embeddable
public class CatRolMenuId implements Serializable {

	/**
	 * 
	 */
	private static final long serialVersionUID = -8010788584109504729L;

    @Column(name = "idrol")
    private int rol;
 
    @Column(name = "idmenu")
    private int menu;
    
    public CatRolMenuId() {  }
    
    public CatRolMenuId(int rol, int menu) {  
    	this.rol = rol;
    	this.menu = menu;
    }

	public int getRol() {
		return rol;
	}

	public void setRol(int rol) {
		this.rol = rol;
	}

	public int getMenu() {
		return menu;
	}

	public void setMenu(int menu) {
		this.menu = menu;
	}

	@Override
	public String toString() {
		return "CatRolMenuId [rol=" + rol + ", menu=" + menu + "]";
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + menu;
		result = prime * result + rol;
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		CatRolMenuId other = (CatRolMenuId) obj;
		if (menu != other.menu)
			return false;
		if (rol != other.rol)
			return false;
		return true;
	}


	
	
    
    

}

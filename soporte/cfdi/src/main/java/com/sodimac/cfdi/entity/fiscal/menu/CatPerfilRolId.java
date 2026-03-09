package com.sodimac.cfdi.entity.fiscal.menu;

import java.io.Serializable;

import javax.persistence.Column;
import javax.persistence.Embeddable;

@Embeddable
public class CatPerfilRolId implements Serializable {

	/**
	 * 
	 */
	private static final long serialVersionUID = -7858542555767755435L;
	
    @Column(name = "idperfil")
    private int perfil;
 
    @Column(name = "idrol")
    private int rol;
    
    public CatPerfilRolId() {}
    
    public CatPerfilRolId(int perfil, int rol) {
    	this.perfil = perfil;
    	this.rol = rol;
    }

	public int getPerfil() {
		return perfil;
	}

	public void setPerfil(int perfil) {
		this.perfil = perfil;
	}

	public int getRol() {
		return rol;
	}

	public void setRol(int rol) {
		this.rol = rol;
	}

	@Override
	public String toString() {
		return "CatPerfilRolId [perfil=" + perfil + ", rol=" + rol + "]";
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + perfil;
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
		CatPerfilRolId other = (CatPerfilRolId) obj;
		if (perfil != other.perfil)
			return false;
		if (rol != other.rol)
			return false;
		return true;
	}

	
    

}

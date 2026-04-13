package com.sodimac.rebates.model.entity;

import java.io.Serializable;
import java.util.Objects;

import javax.persistence.Column;
import javax.persistence.Embeddable;

@Embeddable
public class CatRolPermisoId implements Serializable {

	/**
	 * 
	 */
	private static final long serialVersionUID = -8010788584109504729L;

    @Column(name = "idrol")
    private int rol;
 
    @Column(name = "idpermiso")
    private int permiso;
    
    public CatRolPermisoId() {  }
    
    public CatRolPermisoId(int rol, int permiso) {  
    	this.rol = rol;
    	this.permiso = permiso;
    }

	public int getRol() {
		return rol;
	}

	public void setRol(int rol) {
		this.rol = rol;
	}

	public int getPermiso() {
		return permiso;
	}

	public void setPermiso(int permiso) {
		this.permiso = permiso;
	}

	@Override
	public int hashCode() {
		return Objects.hash(permiso, rol);
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		CatRolPermisoId other = (CatRolPermisoId) obj;
		return permiso == other.permiso && rol == other.rol;
	}
	
	
}

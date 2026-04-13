package com.sodimac.rebates.model.entity;

import java.io.Serializable;

import javax.persistence.EmbeddedId;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.ManyToOne;
import javax.persistence.MapsId;
import javax.persistence.Table;

@Entity
@Table(name = "CatRolPermiso")
public class CatRolPermisoEntity implements Serializable {

	
	/**
	 * 
	 */
	private static final long serialVersionUID = 337802687900465895L;
	
    @EmbeddedId
    private CatRolPermisoId catRolPermisoPk;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @MapsId("idrol")
    private CatRolEntity rol;
 
    @ManyToOne(fetch = FetchType.LAZY)
    @MapsId("idpermiso")
    private CatPermisoEntity permiso;

	public CatRolPermisoEntity() {}
	
	public CatRolPermisoEntity(CatRolEntity rol, CatPermisoEntity permiso) {
		this.rol = rol;
		this.permiso = permiso;
		this.catRolPermisoPk = new CatRolPermisoId(rol.getId(), permiso.getId());
	}
	
	public CatRolPermisoId getCatPerfilRolPk() {
		return catRolPermisoPk;
	}

	public void setCatPerfilRolPk(CatRolPermisoId catRolPermisok) {
		this.catRolPermisoPk = catRolPermisok;
	}

	public CatRolEntity getRol() {
		return rol;
	}

	public void setRol(CatRolEntity rol) {
		this.rol = rol;
	}

	public CatPermisoEntity getPermiso() {
		return permiso;
	}

	public void setPermiso(CatPermisoEntity permiso) {
		this.permiso = permiso;
	}
	
}

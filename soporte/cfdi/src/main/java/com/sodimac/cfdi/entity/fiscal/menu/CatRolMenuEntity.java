package com.sodimac.cfdi.entity.fiscal.menu;

import java.io.Serializable;

import javax.persistence.Column;
import javax.persistence.EmbeddedId;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.ManyToOne;
import javax.persistence.MapsId;
import javax.persistence.Table;
import javax.persistence.Temporal;
import javax.persistence.TemporalType;

import org.hibernate.annotations.CreationTimestamp;

@Entity
@Table(name = "catrolmenu")
public class CatRolMenuEntity implements Serializable {

	
	/**
	 * 
	 */
	private static final long serialVersionUID = 337802687900465895L;
	
    @EmbeddedId
    private CatRolMenuId catPerfilRolPk;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @MapsId("idrol")
    private CatRolEntity rol;
 
    @ManyToOne(fetch = FetchType.LAZY)
    @MapsId("idmenu")
    private CatMenuEntity menu;
	
	@Column(name = "fechaCreacion")
	@CreationTimestamp
	@Temporal(TemporalType.TIMESTAMP)
	private java.util.Date fechaCreacion;
	
	@Column(name = "fechaActualizacion")
	@CreationTimestamp
	@Temporal(TemporalType.TIMESTAMP)
	private java.util.Date fechaActualizacion;

	public CatRolMenuEntity() {}
	
	public CatRolMenuEntity(CatRolEntity rol, CatMenuEntity menu) {
		this.rol = rol;
		this.menu = menu;
		this.catPerfilRolPk = new CatRolMenuId(rol.getId(), menu.getId());
	}
	

	public CatRolMenuId getCatPerfilRolPk() {
		return catPerfilRolPk;
	}

	public void setCatPerfilRolPk(CatRolMenuId catPerfilRolPk) {
		this.catPerfilRolPk = catPerfilRolPk;
	}

	public CatRolEntity getRol() {
		return rol;
	}

	public void setRol(CatRolEntity rol) {
		this.rol = rol;
	}

	public CatMenuEntity getMenu() {
		return menu;
	}

	public void setMenu(CatMenuEntity menu) {
		this.menu = menu;
	}

	public java.util.Date getFechaCreacion() {
		return fechaCreacion;
	}

	public void setFechaCreacion(java.util.Date fechaCreacion) {
		this.fechaCreacion = fechaCreacion;
	}
	
	

	public java.util.Date getFechaActualizacion() {
		return fechaActualizacion;
	}

	public void setFechaActualizacion(java.util.Date fechaActualizacion) {
		this.fechaActualizacion = fechaActualizacion;
	}

	@Override
	public String toString() {
		return "CatRolMenuEntity [catPerfilRolPk=" + catPerfilRolPk + ", rol=" + rol + ", menu=" + menu
				+ ", fechaCreacion=" + fechaCreacion + ", fechaActualizacion=" + fechaActualizacion + "]";
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((catPerfilRolPk == null) ? 0 : catPerfilRolPk.hashCode());
		result = prime * result + ((fechaActualizacion == null) ? 0 : fechaActualizacion.hashCode());
		result = prime * result + ((fechaCreacion == null) ? 0 : fechaCreacion.hashCode());
		result = prime * result + ((menu == null) ? 0 : menu.hashCode());
		result = prime * result + ((rol == null) ? 0 : rol.hashCode());
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
		CatRolMenuEntity other = (CatRolMenuEntity) obj;
		if (catPerfilRolPk == null) {
			if (other.catPerfilRolPk != null)
				return false;
		} else if (!catPerfilRolPk.equals(other.catPerfilRolPk))
			return false;
		if (fechaActualizacion == null) {
			if (other.fechaActualizacion != null)
				return false;
		} else if (!fechaActualizacion.equals(other.fechaActualizacion))
			return false;
		if (fechaCreacion == null) {
			if (other.fechaCreacion != null)
				return false;
		} else if (!fechaCreacion.equals(other.fechaCreacion))
			return false;
		if (menu == null) {
			if (other.menu != null)
				return false;
		} else if (!menu.equals(other.menu))
			return false;
		if (rol == null) {
			if (other.rol != null)
				return false;
		} else if (!rol.equals(other.rol))
			return false;
		return true;
	}

	
	
}

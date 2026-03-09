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
@Table(name = "catperfilrol")
public class CatPerfilRolEntity implements Serializable  {

	/**
	 * 
	 */
	private static final long serialVersionUID = -8828422320929801027L;

	@EmbeddedId
	protected CatPerfilRolId catPerfilRolPk;
	
    @ManyToOne(fetch = FetchType.LAZY)
    @MapsId("idperfil")
    private CatPerfilEntity perfil;
	
    @ManyToOne(fetch = FetchType.LAZY)
    @MapsId("idrol")
    private CatRolEntity rol;
 
	@Column(name = "fechaCreacion")
	@CreationTimestamp
	@Temporal(TemporalType.TIMESTAMP)
	private java.util.Date fechaCreacion;
	
	@Column(name = "fechaActualizacion")
	@CreationTimestamp
	@Temporal(TemporalType.TIMESTAMP)
	private java.util.Date fechaActualizacion;
	
	public CatPerfilRolEntity() {}
	
	public CatPerfilRolEntity(CatPerfilEntity perfil, CatRolEntity rol ) {
		this.perfil = perfil;
		this.rol = rol;
		this.catPerfilRolPk = new CatPerfilRolId(perfil.getId(), rol.getId());
	}


	public CatPerfilRolId getCatPerfilRolPk() {
		return catPerfilRolPk;
	}

	public void setCatPerfilRolPk(CatPerfilRolId catPerfilRolPk) {
		this.catPerfilRolPk = catPerfilRolPk;
	}

	public CatPerfilEntity getPerfil() {
		return perfil;
	}

	public void setPerfil(CatPerfilEntity perfil) {
		this.perfil = perfil;
	}

	public CatRolEntity getRol() {
		return rol;
	}

	public void setRol(CatRolEntity rol) {
		this.rol = rol;
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
		return "CatPerfilRolEntity [catPerfilRolPk=" + catPerfilRolPk + ", perfil=" + perfil + ", rol=" + rol
				+ ", fechaCreacion=" + fechaCreacion + ", fechaActualizacion=" + fechaActualizacion + "]";
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((catPerfilRolPk == null) ? 0 : catPerfilRolPk.hashCode());
		result = prime * result + ((fechaActualizacion == null) ? 0 : fechaActualizacion.hashCode());
		result = prime * result + ((fechaCreacion == null) ? 0 : fechaCreacion.hashCode());
		result = prime * result + ((perfil == null) ? 0 : perfil.hashCode());
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
		CatPerfilRolEntity other = (CatPerfilRolEntity) obj;
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
		if (perfil == null) {
			if (other.perfil != null)
				return false;
		} else if (!perfil.equals(other.perfil))
			return false;
		if (rol == null) {
			if (other.rol != null)
				return false;
		} else if (!rol.equals(other.rol))
			return false;
		return true;
	}

	
	
}

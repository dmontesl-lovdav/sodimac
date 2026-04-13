package com.sodimac.rebates.model.entity;

import java.io.Serializable;
import java.util.Date;

import javax.persistence.Column;
import javax.persistence.EmbeddedId;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.ManyToOne;
import javax.persistence.MapsId;
import javax.persistence.Table;

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
	private Date fechaCreacion;
	
	@Column(name = "fechaActualizacion")
	private Date fechaActualizacion;
	
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

	public Date getFechaCreacion() {
		return fechaCreacion;
	}

	public void setFechaCreacion(Date fechaCreacion) {
		this.fechaCreacion = fechaCreacion;
	}

	public Date getFechaActualizacion() {
		return fechaActualizacion;
	}

	public void setFechaActualizacion(Date fechaActualizacion) {
		this.fechaActualizacion = fechaActualizacion;
	}
}

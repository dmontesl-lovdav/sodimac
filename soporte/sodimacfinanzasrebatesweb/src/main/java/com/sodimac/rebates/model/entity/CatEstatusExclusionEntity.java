package com.sodimac.rebates.model.entity;

import java.io.Serializable;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;

/**
 *
 * @author david.montes
 */
@Entity
@Table(name = "CatEstatusExclusion")
public class CatEstatusExclusionEntity implements Serializable {

	private static final long serialVersionUID = 1L;
	@Id
	@Column(name = "IdCatEstatusExclusion")
	private Integer idCatEstatusExclusion;

	@Column(name = "Clave")
	private String clave;

	@Column(name = "Descripcion")
	private String descripcion;

	@Column(name = "Activo")
	private boolean activo;

	public Integer getIdCatEstatusExclusion() {
		return idCatEstatusExclusion;
	}

	public void setIdCatEstatusExclusion(Integer idCatEstatusExclusion) {
		this.idCatEstatusExclusion = idCatEstatusExclusion;
	}

	public String getClave() {
		return clave;
	}

	public void setClave(String clave) {
		this.clave = clave;
	}

	public String getDescripcion() {
		return descripcion;
	}

	public void setDescripcion(String descripcion) {
		this.descripcion = descripcion;
	}

	public boolean isActivo() {
		return activo;
	}

	public void setActivo(boolean activo) {
		this.activo = activo;
	}

}

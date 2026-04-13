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
@Table(name = "CatTipoExclusion")
public class CatTipoExclusionEntity implements Serializable {

	private static final long serialVersionUID = 1L;

	@Id
	@Column(name = "IdCatTipoExclusion")
	private Integer idCatTipoExclusion;

	@Column(name = "Clave")
	private String clave;

	@Column(name = "Descripcion")
	private String descripcion;

	@Column(name = "Activo")
	private boolean activo;

	public Integer getIdCatTipoExclusion() {
		return idCatTipoExclusion;
	}

	public void setIdCatTipoExclusion(Integer idCatTipoExclusion) {
		this.idCatTipoExclusion = idCatTipoExclusion;
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

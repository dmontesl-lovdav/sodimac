package com.sodimac.rebates.model.entity;

import java.io.Serializable;
import java.util.List;

import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.OneToMany;
import javax.persistence.Table;

/**
 *
 * @author david.montes
 */
@Entity
@Table(name = "CatEvento")
public class CatEventoEntity implements Serializable {

	private static final long serialVersionUID = 1L;
	@Id
	@Column(name = "IdCatEvento")
	private Integer idCatEvento;

	@Column(name = "Clave")
	private String clave;

	@Column(name = "Descripcion")
	private String descripcion;

	@Column(name = "Activo")
	private boolean activo;

	@OneToMany(cascade = CascadeType.ALL, mappedBy = "catEventoEntity")
	private List<EventoPermisoRolEntity> eventoPermisoRolEntityList;

	public CatEventoEntity() {
	}

	public CatEventoEntity(Integer idCatEvento) {
		this.idCatEvento = idCatEvento;
	}

	public CatEventoEntity(Integer idCatEvento, String descripcion, boolean activo) {
		this.idCatEvento = idCatEvento;
		this.descripcion = descripcion;
		this.activo = activo;
	}

	public Integer getIdCatEvento() {
		return idCatEvento;
	}

	public void setIdCatEvento(Integer idCatEvento) {
		this.idCatEvento = idCatEvento;
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

	public boolean getActivo() {
		return activo;
	}

	public void setActivo(boolean activo) {
		this.activo = activo;
	}

	public List<EventoPermisoRolEntity> getEventoPermisoRolEntityList() {
		return eventoPermisoRolEntityList;
	}

	public void setEventoPermisoRolEntityList(List<EventoPermisoRolEntity> eventoPermisoRolEntityList) {
		this.eventoPermisoRolEntityList = eventoPermisoRolEntityList;
	}

	@Override
	public int hashCode() {
		int hash = 0;
		hash += (idCatEvento != null ? idCatEvento.hashCode() : 0);
		return hash;
	}

	@Override
	public boolean equals(Object object) {
		if (!(object instanceof CatEventoEntity)) {
			return false;
		}
		CatEventoEntity other = (CatEventoEntity) object;
		if ((this.idCatEvento == null && other.idCatEvento != null)
				|| (this.idCatEvento != null && !this.idCatEvento.equals(other.idCatEvento))) {
			return false;
		}
		return true;
	}

	@Override
	public String toString() {
		return "com.sodimac.rebates.model.entity.CatEventoEntity[ idCatEvento=" + idCatEvento + " ]";
	}

}

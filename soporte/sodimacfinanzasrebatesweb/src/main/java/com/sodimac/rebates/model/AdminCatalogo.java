package com.sodimac.rebates.model;

import java.util.Date;

import javax.persistence.EmbeddedId;

import javax.persistence.Entity;
import javax.persistence.Table;

@Entity
@Table(name = "adminCatalogo")
public class AdminCatalogo {

	@EmbeddedId
	private CatalogoId catalogoId;
	private String elemento;
	private String descripcion;
	private Integer usuarioCreacion;
	private Integer usuarioActualizacion;
	private Date fechaCreacion;
	private Date fechaActualiizacion;
	private boolean activo;

	public CatalogoId getCatalogoId() {
		return catalogoId;
	}

	public void setCatalogoId(CatalogoId catalogoId) {
		this.catalogoId = catalogoId;
	}

	public Date getFechaActualiizacion() {
		return fechaActualiizacion;
	}

	public void setFechaActualiizacion(Date fechaActualiizacion) {
		this.fechaActualiizacion = fechaActualiizacion;
	}

	public String getElemento() {
		return elemento;
	}

	public void setElemento(String elemento) {
		this.elemento = elemento;
	}

	public String getDescripcion() {
		return descripcion;
	}

	public void setDescripcion(String descripcion) {
		this.descripcion = descripcion;
	}

	public Integer getUsuarioCreacion() {
		return usuarioCreacion;
	}

	public void setUsuarioCreacion(Integer usuarioCreacion) {
		this.usuarioCreacion = usuarioCreacion;
	}

	public Integer getUsuarioActualizacion() {
		return usuarioActualizacion;
	}

	public void setUsuarioActualizacion(Integer usuarioActualizacion) {
		this.usuarioActualizacion = usuarioActualizacion;
	}

	public Date getFechaCreacion() {
		return fechaCreacion;
	}

	public void setFechaCreacion(Date fechaCreacion) {
		this.fechaCreacion = fechaCreacion;
	}

	public Date getFechaActualizacion() {
		return fechaActualiizacion;
	}

	public void setFechaActualizacion(Date fechaActualizacion) {
		this.fechaActualiizacion = fechaActualizacion;
	}

	public boolean isActivo() {
		return activo;
	}

	public void setActivo(boolean activo) {
		this.activo = activo;
	}

	@Override
	public String toString() {
		return "AdminCatalogo [catalogoId=" + catalogoId + ", elemento=" + elemento + ", descripcion=" + descripcion
				+ ", usuarioCreacion=" + usuarioCreacion + ", usuarioActualizacion=" + usuarioActualizacion
				+ ", fechaCreacion=" + fechaCreacion + ", fechaActualiizacion=" + fechaActualiizacion + ", activo="
				+ activo + "]";
	}

}

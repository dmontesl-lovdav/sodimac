package com.sodimac.rebates.model;

import java.util.Date;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Table;

import org.hibernate.annotations.Filter;
import org.hibernate.annotations.FilterDef;
import org.hibernate.annotations.Where;


@Entity
@Table(name = "catCatalogo")
public class Catalogo {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Integer idCatalogo;
	private String nombre;
	private String descripcion;
	private Integer usuarioCreacion;
	private Integer usuarioActualizacion;
	private Date fechaCreacion;
	private Date fechaActualizacion;
	private boolean activo;
	private boolean logicActive;

	public Catalogo() {}
	
	public Catalogo(String nombre, String descripcion, boolean activo, Integer usuarioCreacion) {
		this.nombre = nombre;
		this.descripcion = descripcion;
		this.activo = activo;
		this.fechaCreacion = new Date();
		this.usuarioCreacion = usuarioCreacion;
	}
	
	public Integer getIdCatalogo() {
		return idCatalogo;
	}

	public void setIdCatalogo(Integer idCatalogo) {
		this.idCatalogo = idCatalogo;
	}

	public String getNombre() {
		return nombre;
	}

	public void setNombre(String nombre) {
		this.nombre = nombre;
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
		return fechaActualizacion;
	}

	public void setFechaActualizacion(Date fechaActualizacion) {
		this.fechaActualizacion = fechaActualizacion;
	}

	public boolean isActivo() {
		return activo;
	}

	public void setActivo(boolean activo) {
		this.activo = activo;
	}

	public boolean isLogicActive() {
		return logicActive;
	}

	public void setLogicActive(boolean logicActive) {
		this.logicActive = logicActive;
	}

	@Override
	public String toString() {
		return "Catalogo [idCatalogo=" + idCatalogo + ", nombre=" + nombre + ", descripcion=" + descripcion
				+ ", usuarioCreacion=" + usuarioCreacion + ", usuarioActualizacion=" + usuarioActualizacion
				+ ", fechaCreacion=" + fechaCreacion + ", fechaActualizacion=" + fechaActualizacion + ", activo="
				+ activo + "]";
	}

}

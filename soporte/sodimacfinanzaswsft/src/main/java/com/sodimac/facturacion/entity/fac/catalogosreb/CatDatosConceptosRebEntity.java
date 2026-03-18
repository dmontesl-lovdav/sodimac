package com.sodimac.facturacion.entity.fac.catalogosreb;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;
import javax.persistence.Temporal;
import javax.persistence.TemporalType;

import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.Type;

@Entity
@Table(name = "catDatosConceptosReb")
public class CatDatosConceptosRebEntity {

	@Id
	@Column(name = "id")
	private int id;

	@Column(name = "claveProdServ")
	private String claveProdServ;

	@Column(name = "noIdentificacion")
	private String noIdentificacion;

	@Column(name = "cantidad", columnDefinition = "decimal(12,2)")
	private double cantidad;

	@Column(name = "claveUnidad")
	private String claveUnidad;

	@Column(name = "unidad")
	private String unidad;

	@Column(name = "descripcion")
	private String descripcion;

	@Column(name = "idRebate")
	private int idRebate;
	
	@Column(name = "activo", nullable = false, columnDefinition = "BIT", length = 1)
	@Type(type = "org.hibernate.type.NumericBooleanType")
	private boolean activo = true;

	@Column(name = "fechaCreacion")
	@CreationTimestamp
	@Temporal(TemporalType.TIMESTAMP)
	private java.util.Date fechaCreacion;

	@Column(name = "fechaModificacion")
	private java.util.Date fechaModificacion;

	public CatDatosConceptosRebEntity() {

	}

	public int getId() {
		return id;
	}

	public void setId(int id) {
		this.id = id;
	}

	public String getClaveProdServ() {
		return claveProdServ;
	}

	public void setClaveProdServ(String claveProdServ) {
		this.claveProdServ = claveProdServ;
	}

	public String getNoIdentificacion() {
		return noIdentificacion;
	}

	public void setNoIdentificacion(String noIdentificacion) {
		this.noIdentificacion = noIdentificacion;
	}

	public double getCantidad() {
		return cantidad;
	}

	public void setCantidad(double cantidad) {
		this.cantidad = cantidad;
	}

	public String getClaveUnidad() {
		return claveUnidad;
	}

	public void setClaveUnidad(String claveUnidad) {
		this.claveUnidad = claveUnidad;
	}

	public String getUnidad() {
		return unidad;
	}

	public void setUnidad(String unidad) {
		this.unidad = unidad;
	}

	public String getDescripcion() {
		return descripcion;
	}

	public void setDescripcion(String descripcion) {
		this.descripcion = descripcion;
	}

	public int getIdRebate() {
		return idRebate;
	}

	public void setIdRebate(int idRebate) {
		this.idRebate = idRebate;
	}

	public boolean isActivo() {
		return activo;
	}

	public void setActivo(boolean activo) {
		this.activo = activo;
	}

	public java.util.Date getFechaCreacion() {
		return fechaCreacion;
	}

	public void setFechaCreacion(java.util.Date fechaCreacion) {
		this.fechaCreacion = fechaCreacion;
	}

	public java.util.Date getFechaModificacion() {
		return fechaModificacion;
	}

	public void setFechaModificacion(java.util.Date fechaModificacion) {
		this.fechaModificacion = fechaModificacion;
	}

	@Override
	public String toString() {
		return "CatDatosConceptosRebEntity [id=" + id + ", claveProdServ=" + claveProdServ + ", noIdentificacion="
				+ noIdentificacion + ", cantidad=" + cantidad + ", claveUnidad=" + claveUnidad + ", unidad=" + unidad
				+ ", descripcion=" + descripcion + ", idRebate=" + idRebate + ", activo=" + activo + ", fechaCreacion="
				+ fechaCreacion + ", fechaModificacion=" + fechaModificacion + "]";
	}

}

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
@Table(name = "catDatosImpuestosReb")
public class CatDatosImpuestosRebEntity {

	@Id
	@Column(name = "id")
	private int id;

	@Column(name = "impuesto")
	private String impuesto;

	@Column(name = "tipoFactor")
	private String tipoFactor;

	@Column(name = "tasaCuota", columnDefinition = "decimal(7,6)")
	private double tasaCuota;

	@Column(name = "ordenador")
	private int ordenador;

	@Column(name = "descripcion")
	private String descripcion;

	@Column(name = "fechaVigencia")
	private java.util.Date fechaVigencia;

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

	public CatDatosImpuestosRebEntity() {

	}

	public int getId() {
		return id;
	}

	public void setId(int id) {
		this.id = id;
	}

	public String getImpuesto() {
		return impuesto;
	}

	public void setImpuesto(String impuesto) {
		this.impuesto = impuesto;
	}

	public String getTipoFactor() {
		return tipoFactor;
	}

	public void setTipoFactor(String tipoFactor) {
		this.tipoFactor = tipoFactor;
	}

	public double getTasaCuota() {
		return tasaCuota;
	}

	public void setTasaCuota(double tasaCuota) {
		this.tasaCuota = tasaCuota;
	}

	public int getOrdenador() {
		return ordenador;
	}

	public void setOrdenador(int ordenador) {
		this.ordenador = ordenador;
	}

	public String getDescripcion() {
		return descripcion;
	}

	public void setDescripcion(String descripcion) {
		this.descripcion = descripcion;
	}

	public java.util.Date getFechaVigencia() {
		return fechaVigencia;
	}

	public void setFechaVigencia(java.util.Date fechaVigencia) {
		this.fechaVigencia = fechaVigencia;
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
		return "CatDatosImpuestosRebEntity [id=" + id + ", impuesto=" + impuesto + ", tipoFactor=" + tipoFactor
				+ ", tasaCuota=" + tasaCuota + ", ordenador=" + ordenador + ", descripcion=" + descripcion
				+ ", fechaVigencia=" + fechaVigencia + ", idRebate=" + idRebate + ", activo=" + activo
				+ ", fechaCreacion=" + fechaCreacion + ", fechaModificacion=" + fechaModificacion + "]";
	}

}

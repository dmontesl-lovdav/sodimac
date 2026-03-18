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
@Table(name = "confDatosEmisorReb")
public class ConfDatosEmisorRebEntity {

	@Id
	@Column(name = "id")
	private int id;

	@Column(name = "rfc")
	private String rfc;

	@Column(name = "razonSocial")
	private String razonSocial;

	@Column(name = "regimenFiscal")
	private String regimenFiscal;

	@Column(name = "empresa")
	private String empresa;

	@Column(name = "formaPago")
	private String formaPago;

	@Column(name = "folio")
	private int folio;

	@Column(name = "lugarExpedicion")
	private String lugarExpedicion;

	@Column(name = "metodoPago")
	private String metodoPago;

	@Column(name = "serie")
	private String serie;

	@Column(name = "activo", nullable = false, columnDefinition = "BIT", length = 1)
	@Type(type = "org.hibernate.type.NumericBooleanType")
	private boolean activo = true;

	@Column(name = "fechaCreacion")
	@CreationTimestamp
	@Temporal(TemporalType.TIMESTAMP)
	private java.util.Date fechaCreacion;

	@Column(name = "fechaModificacion")
	private java.util.Date fechaModificacion;

	public ConfDatosEmisorRebEntity() {

	}

	public int getId() {
		return id;
	}

	public void setId(int id) {
		this.id = id;
	}

	public String getRfc() {
		return rfc;
	}

	public void setRfc(String rfc) {
		this.rfc = rfc;
	}

	public String getRazonSocial() {
		return razonSocial;
	}

	public void setRazonSocial(String razonSocial) {
		this.razonSocial = razonSocial;
	}

	public String getRegimenFiscal() {
		return regimenFiscal;
	}

	public void setRegimenFiscal(String regimenFiscal) {
		this.regimenFiscal = regimenFiscal;
	}

	public String getEmpresa() {
		return empresa;
	}

	public void setEmpresa(String empresa) {
		this.empresa = empresa;
	}

	public String getFormaPago() {
		return formaPago;
	}

	public void setFormaPago(String formaPago) {
		this.formaPago = formaPago;
	}

	public int getFolio() {
		return folio;
	}

	public void setFolio(int folio) {
		this.folio = folio;
	}

	public String getLugarExpedicion() {
		return lugarExpedicion;
	}

	public void setLugarExpedicion(String lugarExpedicion) {
		this.lugarExpedicion = lugarExpedicion;
	}

	public String getMetodoPago() {
		return metodoPago;
	}

	public void setMetodoPago(String metodoPago) {
		this.metodoPago = metodoPago;
	}

	public String getSerie() {
		return serie;
	}

	public void setSerie(String serie) {
		this.serie = serie;
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
		return "ConfDatosEmisorRebEntity [id=" + id + ", rfc=" + rfc + ", razonSocial=" + razonSocial
				+ ", regimenFiscal=" + regimenFiscal + ", empresa=" + empresa + ", formaPago=" + formaPago + ", folio="
				+ folio + ", lugarExpedicion=" + lugarExpedicion + ", metodoPago=" + metodoPago + ", serie=" + serie
				+ ", activo=" + activo + ", fechaCreacion=" + fechaCreacion + ", fechaModificacion=" + fechaModificacion
				+ "]";
	}

}

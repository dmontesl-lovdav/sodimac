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
@Table(name = "catDatosControlReb")
public class CatDatosControlRebEntity {

	@Id
	@Column(name = "id")
	private int id;

	@Column(name = "cfdId")
	private String cfdId;

	@Column(name = "estatusId")
	private String estatusId;

	@Column(name = "estatusIdImpresion")
	private String estatusIdImpresion;

	@Column(name = "estatusIdCorreo")
	private String estatusIdCorreo;

	@Column(name = "estatusIdArchivo")
	private String estatusIdArchivo;

	@Column(name = "rechazoId")
	private String rechazoId;
	
	@Column(name = "complemenotId")
	private String complemenotId;

	@Column(name = "activo", nullable = false, columnDefinition = "BIT", length = 1)
	@Type(type = "org.hibernate.type.NumericBooleanType")
	private boolean activo = true;

	@Column(name = "fechaCreacion")
	@CreationTimestamp
	@Temporal(TemporalType.TIMESTAMP)
	private java.util.Date fechaCreacion;

	@Column(name = "fechaModificacion")
	private java.util.Date fechaModificacion;

	public CatDatosControlRebEntity() {

	}

	public int getId() {
		return id;
	}

	public void setId(int id) {
		this.id = id;
	}

	public String getCfdId() {
		return cfdId;
	}

	public void setCfdId(String cfdId) {
		this.cfdId = cfdId;
	}

	public String getEstatusId() {
		return estatusId;
	}

	public void setEstatusId(String estatusId) {
		this.estatusId = estatusId;
	}

	public String getEstatusIdImpresion() {
		return estatusIdImpresion;
	}

	public void setEstatusIdImpresion(String estatusIdImpresion) {
		this.estatusIdImpresion = estatusIdImpresion;
	}

	public String getEstatusIdCorreo() {
		return estatusIdCorreo;
	}

	public void setEstatusIdCorreo(String estatusIdCorreo) {
		this.estatusIdCorreo = estatusIdCorreo;
	}

	public String getEstatusIdArchivo() {
		return estatusIdArchivo;
	}

	public void setEstatusIdArchivo(String estatusIdArchivo) {
		this.estatusIdArchivo = estatusIdArchivo;
	}

	public String getRechazoId() {
		return rechazoId;
	}

	public void setRechazoId(String rechazoId) {
		this.rechazoId = rechazoId;
	}

	public String getComplemenotId() {
		return complemenotId;
	}

	public void setComplemenotId(String complemenotId) {
		this.complemenotId = complemenotId;
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
		return "CatDatosControlRebEntity [id=" + id + ", cfdId=" + cfdId + ", estatusId=" + estatusId
				+ ", estatusIdImpresion=" + estatusIdImpresion + ", estatusIdCorreo=" + estatusIdCorreo
				+ ", estatusIdArchivo=" + estatusIdArchivo + ", rechazoId=" + rechazoId + ", complemenotId="
				+ complemenotId + ", activo=" + activo + ", fechaCreacion=" + fechaCreacion + ", fechaModificacion="
				+ fechaModificacion + "]";
	}

}

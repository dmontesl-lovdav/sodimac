package com.sodimac.rebates.model;

import java.util.Date;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Table;

@Entity
@Table(name = "catDocumento")
public class Documento {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Integer idDocumento;
	private String nombreDocumento;
	private String rutaDocumento;
	private Integer usuario;
	private Date fechaRegistro;
	private Date fechaActualizacion;
	private String extension;
	private String nomenclatura;
	private Integer numeroCarga;
	private boolean requerido;
	private boolean activo;
	private Integer periodoComun;

	public Integer getIdDocumento() {
		return idDocumento;
	}

	public void setIdDocumento(Integer idDocumento) {
		this.idDocumento = idDocumento;
	}

	public String getNombreDocumento() {
		return nombreDocumento;
	}

	public void setNombreDocumento(String nombreDocumento) {
		this.nombreDocumento = nombreDocumento;
	}

	public String getRutaDocumento() {
		return rutaDocumento;
	}

	public void setRutaDocumento(String rutaDocumento) {
		this.rutaDocumento = rutaDocumento;
	}

	public Integer getUsuario() {
		return usuario;
	}

	public void setUsuario(Integer usuario) {
		this.usuario = usuario;
	}

	public Date getFechaRegistro() {
		return fechaRegistro;
	}

	public void setFechaRegistro(Date fechaRegistro) {
		this.fechaRegistro = fechaRegistro;
	}

	public Date getFechaActualizacion() {
		return fechaActualizacion;
	}

	public void setFechaActualizacion(Date fechaActualizacion) {
		this.fechaActualizacion = fechaActualizacion;
	}

	public String getExtension() {
		return extension;
	}

	public void setExtension(String extension) {
		this.extension = extension;
	}

	public String getNomenclatura() {
		return nomenclatura;
	}

	public void setNomenclatura(String nomenclatura) {
		this.nomenclatura = nomenclatura;
	}

	public Integer getNumeroCarga() {
		return numeroCarga;
	}

	public void setNumeroCarga(Integer numeroCarga) {
		this.numeroCarga = numeroCarga;
	}

	public boolean isRequerido() {
		return requerido;
	}

	public void setRequerido(boolean requerido) {
		this.requerido = requerido;
	}

	public boolean isActivo() {
		return activo;
	}

	public void setActivo(boolean activo) {
		this.activo = activo;
	}
	
	public Integer getPeriodoComun() {
		return periodoComun;
	}

	public void setPeriodoComun(Integer periodoComun) {
		this.periodoComun = periodoComun;
	}

	@Override
	public String toString() {
		return "Documento [idDocumento=" + idDocumento + ", nombreDocumento=" + nombreDocumento + ", rutaDocumento="
				+ rutaDocumento + ", usuario=" + usuario + ", fechaRegistro=" + fechaRegistro + ", fechaActualizacion="
				+ fechaActualizacion + ", extension=" + extension + ", nomenclatura=" + nomenclatura + ", numeroCarga="
				+ numeroCarga + ", requerido=" + requerido + ", activo=" + activo + "]";
	}

}

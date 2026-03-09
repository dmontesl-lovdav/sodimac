package com.sodimac.cfdi.entity.fiscal;

import java.util.Date;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;
import javax.persistence.Temporal;
import javax.persistence.TemporalType;

import org.hibernate.annotations.CreationTimestamp;

@Entity
@Table(name = "catdocumentoconf")
public class CatDocumentoConfEntity {

	@Id
	@Column(name = "idDocumentoConf")
	private Integer idDocumentoConf;

	@Column(name = "idDocumentoCabecera")
	private Integer idDocumentoCabecera;

	@Column(name = "idConfiguracionFtp")
	private Integer idConfiguracionFtp;

	@Column(name = "nombreConfiguracion")
	private String nombreConfiguracion;

	@Column(name = "extension")
	private String extension;

	@Column(name = "rutaDeposito")
	private String rutaDeposito;

	@Column(name = "estatus")
	private Integer estatus;

	@Column(name = "fechaCreacion")
	@CreationTimestamp
	@Temporal(TemporalType.TIMESTAMP)
	private Date fechaCreacion;

	public Integer getIdDocumentoConf() {
		return idDocumentoConf;
	}

	public void setIdDocumentoConf(Integer idDocumentoConf) {
		this.idDocumentoConf = idDocumentoConf;
	}

	public Integer getIdDocumentoCabecera() {
		return idDocumentoCabecera;
	}

	public void setIdDocumentoCabecera(Integer idDocumentoCabecera) {
		this.idDocumentoCabecera = idDocumentoCabecera;
	}

	public Integer getIdConfiguracionFtp() {
		return idConfiguracionFtp;
	}

	public void setIdConfiguracionFtp(Integer idConfiguracionFtp) {
		this.idConfiguracionFtp = idConfiguracionFtp;
	}

	public String getNombreConfiguracion() {
		return nombreConfiguracion;
	}

	public void setNombreConfiguracion(String nombreConfiguracion) {
		this.nombreConfiguracion = nombreConfiguracion;
	}

	public String getExtension() {
		return extension;
	}

	public void setExtension(String extension) {
		this.extension = extension;
	}

	public String getRutaDeposito() {
		return rutaDeposito;
	}

	public void setRutaDeposito(String rutaDeposito) {
		this.rutaDeposito = rutaDeposito;
	}

	public Integer getEstatus() {
		return estatus;
	}

	public void setEstatus(Integer estatus) {
		this.estatus = estatus;
	}

	public Date getFechaCreacion() {
		return fechaCreacion;
	}

	public void setFechaCreacion(Date fechaCreacion) {
		this.fechaCreacion = fechaCreacion;
	}

}

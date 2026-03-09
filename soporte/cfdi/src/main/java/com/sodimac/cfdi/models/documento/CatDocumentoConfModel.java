package com.sodimac.cfdi.models.documento;

import java.util.Date;

public class CatDocumentoConfModel {

	private Integer idDocumentoConf;
	private Integer idDocumentoCabecera;
	private Integer idConfiguracionFtp;
	private String nombreConfiguracion;
	private String extension;
	private String rutaDeposito;
	private Integer estatus;
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

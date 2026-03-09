package com.sodimac.cfdi.models.documento;

import java.util.Date;

public class CatDocumentoCabeceraModel {

	private Integer idDocumentoCabecera;
	private Integer idTipoDocumento;
	private Integer numeroColumnas;
	private Integer filaComienza;
	private String caracterSeparador;
	private Integer estatus;
	private Date fechaCreacion;

	public Integer getIdDocumentoCabecera() {
		return idDocumentoCabecera;
	}

	public void setIdDocumentoCabecera(Integer idDocumentoCabecera) {
		this.idDocumentoCabecera = idDocumentoCabecera;
	}

	public Integer getIdTipoDocumento() {
		return idTipoDocumento;
	}

	public void setIdTipoDocumento(Integer idTipoDocumento) {
		this.idTipoDocumento = idTipoDocumento;
	}

	public Integer getNumeroColumnas() {
		return numeroColumnas;
	}

	public void setNumeroColumnas(Integer numeroColumnas) {
		this.numeroColumnas = numeroColumnas;
	}

	public Integer getFilaComienza() {
		return filaComienza;
	}

	public void setFilaComienza(Integer filaComienza) {
		this.filaComienza = filaComienza;
	}

	public String getCaracterSeparador() {
		return caracterSeparador;
	}

	public void setCaracterSeparador(String caracterSeparador) {
		this.caracterSeparador = caracterSeparador;
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

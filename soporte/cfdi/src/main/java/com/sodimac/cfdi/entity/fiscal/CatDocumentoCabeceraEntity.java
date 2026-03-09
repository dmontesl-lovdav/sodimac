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
@Table(name = "catdocumentocabecera")
public class CatDocumentoCabeceraEntity {

	@Id
	@Column(name = "idDocumentoCabecera")
	private Integer idDocumentoCabecera;

	@Column(name = "idTipoDocumento")
	private Integer idTipoDocumento;

	@Column(name = "numeroColumnas")
	private Integer numeroColumnas;

	@Column(name = "filaComienza")
	private Integer filaComienza;

	@Column(name = "caracterSeparador")
	private String caracterSeparador;

	@Column(name = "estatus")
	private Integer estatus;

	@Column(name = "fechaCreacion")
	@CreationTimestamp
	@Temporal(TemporalType.TIMESTAMP)
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

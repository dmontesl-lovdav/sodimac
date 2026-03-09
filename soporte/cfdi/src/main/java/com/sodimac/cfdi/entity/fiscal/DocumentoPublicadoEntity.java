package com.sodimac.cfdi.entity.fiscal;

import java.util.Date;

import javax.persistence.Basic;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Table;
import javax.persistence.Temporal;
import javax.persistence.TemporalType;

import org.hibernate.annotations.CreationTimestamp;

@Entity
@Table(name = "documentopublicado")
public class DocumentoPublicadoEntity {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	@Basic(optional = false)
    @Column(name = "idDocumentoPublicado",unique=true, nullable = false)
	private Integer idDocumentoPublicado;
	
	@Column(name = "idEstatusDocumento")
	private Integer idEstatusDocumento;

	@Column(name = "idDocumentoConf")
	private Integer idDocumentoConf;

	@Column(name = "idMensaje")
	private Integer idMensaje;

	@Column(name = "nombreArchivo")
	private String nombreArchivo;

	@Column(name = "numeroRegistros")
	private Integer numeroRegistros;

	@Column(name = "peso")
	private Double peso;

	@Column(name = "estatus")
	private Integer estatus;

	@Column(name = "fechaCreacion")
	@CreationTimestamp
	@Temporal(TemporalType.TIMESTAMP)
	private Date fechaCreacion;

	@Column(name = "fechaPublicacion")
	@CreationTimestamp
	@Temporal(TemporalType.TIMESTAMP)
	private Date fechaPublicacion;

	@Column(name = "fechaActualizacion")
	@CreationTimestamp
	@Temporal(TemporalType.TIMESTAMP)
	private Date fechaActualizacion;

	@Column(name = "usuarioCreacion")
	private Integer usuarioCreacion;

	public Integer getIdDocumentoPublicado() {
		return idDocumentoPublicado;
	}

	public void setIdDocumentoPublicado(Integer idDocumentoPublicado) {
		this.idDocumentoPublicado = idDocumentoPublicado;
	}

	public Integer getIdEstatusDocumento() {
		return idEstatusDocumento;
	}

	public void setIdEstatusDocumento(Integer idEstatusDocumento) {
		this.idEstatusDocumento = idEstatusDocumento;
	}

	public Integer getIdDocumentoConf() {
		return idDocumentoConf;
	}

	public void setIdDocumentoConf(Integer idDocumentoConf) {
		this.idDocumentoConf = idDocumentoConf;
	}

	public Integer getIdMensaje() {
		return idMensaje;
	}

	public void setIdMensaje(Integer idMensaje) {
		this.idMensaje = idMensaje;
	}

	public String getNombreArchivo() {
		return nombreArchivo;
	}

	public void setNombreArchivo(String nombreArchivo) {
		this.nombreArchivo = nombreArchivo;
	}

	public Integer getNumeroRegistros() {
		return numeroRegistros;
	}

	public void setNumeroRegistros(Integer numeroRegistros) {
		this.numeroRegistros = numeroRegistros;
	}

	public Double getPeso() {
		return peso;
	}

	public void setPeso(Double peso) {
		this.peso = peso;
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

	public Date getFechaPublicacion() {
		return fechaPublicacion;
	}

	public void setFechaPublicacion(Date fechaPublicacion) {
		this.fechaPublicacion = fechaPublicacion;
	}

	public Date getFechaActualizacion() {
		return fechaActualizacion;
	}

	public void setFechaActualizacion(Date fechaActualizacion) {
		this.fechaActualizacion = fechaActualizacion;
	}

	public Integer getUsuarioCreacion() {
		return usuarioCreacion;
	}

	public void setUsuarioCreacion(Integer usuarioCreacion) {
		this.usuarioCreacion = usuarioCreacion;
	}

}

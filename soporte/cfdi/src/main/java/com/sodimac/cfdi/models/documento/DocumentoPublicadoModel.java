package com.sodimac.cfdi.models.documento;

public class DocumentoPublicadoModel {

	private Integer idDocumentoPublicado;
	private Integer idEstatusDocumento;
	private Integer idDocumentoConf;
	private Integer idMensaje;
	private String nombreArchivo;
	private Integer numeroRegistros;
	private String numeroRegistrosStr;
	private String peso;
	private Integer estatus;
	private String fechaCreacion;
	private String fechaPublicacion;
	private String fechaActualizacion;
	private Integer idUsuarioCreacion;
	private String mensajeDescripcion;
	private String tipoDocumento;
	private String estatusDocumento;

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

	public String getNumeroRegistrosStr() {
		return numeroRegistrosStr;
	}

	public void setNumeroRegistrosStr(String numeroRegistrosStr) {
		this.numeroRegistrosStr = numeroRegistrosStr;
	}

	public String getPeso() {
		return peso;
	}

	public void setPeso(String peso) {
		this.peso = peso;
	}

	public Integer getEstatus() {
		return estatus;
	}

	public void setEstatus(Integer estatus) {
		this.estatus = estatus;
	}

	public String getFechaCreacion() {
		return fechaCreacion;
	}

	public void setFechaCreacion(String fechaCreacion) {
		this.fechaCreacion = fechaCreacion;
	}

	public String getFechaPublicacion() {
		return fechaPublicacion;
	}

	public void setFechaPublicacion(String fechaPublicacion) {
		this.fechaPublicacion = fechaPublicacion;
	}

	public String getFechaActualizacion() {
		return fechaActualizacion;
	}

	public void setFechaActualizacion(String fechaActualizacion) {
		this.fechaActualizacion = fechaActualizacion;
	}

	public Integer getIdUsuarioCreacion() {
		return idUsuarioCreacion;
	}

	public void setIdUsuarioCreacion(Integer idUsuarioCreacion) {
		this.idUsuarioCreacion = idUsuarioCreacion;
	}

	public String getMensajeDescripcion() {
		return mensajeDescripcion;
	}

	public void setMensajeDescripcion(String mensajeDescripcion) {
		this.mensajeDescripcion = mensajeDescripcion;
	}

	public String getTipoDocumento() {
		return tipoDocumento;
	}

	public void setTipoDocumento(String tipoDocumento) {
		this.tipoDocumento = tipoDocumento;
	}

	public String getEstatusDocumento() {
		return estatusDocumento;
	}

	public void setEstatusDocumento(String estatusDocumento) {
		this.estatusDocumento = estatusDocumento;
	}
}

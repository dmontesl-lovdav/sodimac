package com.sodimac.cfdi.models;

public class ProcesoDescargaModel {
	
	private String idEjecucion;
	private String fechaSolicitud;
	private String fechaGeneracion;
	private String parametros;
	private String modulo;
	private String usuario;
	private Integer estatus;
	private String mensaje;
	private String listaArchivos;
	
	public String getIdEjecucion() {
		return idEjecucion;
	}
	public void setIdEjecucion(String idEjecucion) {
		this.idEjecucion = idEjecucion;
	}
	public String getFechaSolicitud() {
		return fechaSolicitud;
	}
	public void setFechaSolicitud(String fechaSolicitud) {
		this.fechaSolicitud = fechaSolicitud;
	}
	public String getFechaGeneracion() {
		return fechaGeneracion;
	}
	public void setFechaGeneracion(String fechaGeneracion) {
		this.fechaGeneracion = fechaGeneracion;
	}
	public String getParametros() {
		return parametros;
	}
	public void setParametros(String parametros) {
		this.parametros = parametros;
	}
	public String getModulo() {
		return modulo;
	}
	public void setModulo(String modulo) {
		this.modulo = modulo;
	}
	public String getUsuario() {
		return usuario;
	}
	public void setUsuario(String usuario) {
		this.usuario = usuario;
	}
	public Integer getEstatus() {
		return estatus;
	}
	public void setEstatus(Integer estatus) {
		this.estatus = estatus;
	}
	public String getMensaje() {
		return mensaje;
	}
	public void setMensaje(String mensaje) {
		this.mensaje = mensaje;
	}
	public String getListaArchivos() {
		return listaArchivos;
	}
	public void setListaArchivos(String listaArchivos) {
		this.listaArchivos = listaArchivos;
	}
	
	
}

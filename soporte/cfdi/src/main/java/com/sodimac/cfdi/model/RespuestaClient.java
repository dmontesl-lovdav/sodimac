package com.sodimac.cfdi.model;

public class RespuestaClient {

	protected String codigo;
	protected String descripcion;
	
	public RespuestaClient() {}
	
	public RespuestaClient(String codigo, String descripcion) {
		this.codigo = codigo;
		this.descripcion = descripcion;
	}
	
	public String getCodigo() {
		return codigo;
	}

	public void setCodigo(String value) {
		this.codigo = value;
	}

	public String getDescripcion() {
		return descripcion;
	}

	public void setDescripcion(String value) {
		this.descripcion = value;
	}

}

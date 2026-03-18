package com.sodimac.facturacion.clientews.configuracion;

public class ResponseBaseDto {

    protected String codigo;
    protected String descripcion;
    
    public ResponseBaseDto() {
    	this.codigo = "1";
    	this.descripcion = "Ok";
    }
    
    
	public String getCodigo() {
		return codigo;
	}
	public void setCodigo(String codigo) {
		this.codigo = codigo;
	}
	public String getDescripcion() {
		return descripcion;
	}
	public void setDescripcion(String descripcion) {
		this.descripcion = descripcion;
	}
    
}

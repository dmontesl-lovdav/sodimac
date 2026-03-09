package com.sodimac.cfdi.cliente.wsadministracion;

public class ResponseBaseDto {

	 protected String codigo;
	    protected String descripcion;
	    
	    public ResponseBaseDto() {
//	    	this.codigo = Integer.toString(ECodigo.Ok.getValor());
//	    	this.descripcion = ECodigo.Ok.message();
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

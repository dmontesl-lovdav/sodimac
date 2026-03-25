package com.sodimac.facturacion.models;

public class ErrorBase {

	private String codigoError;

	private String mensajeError;

    public ErrorBase(){
        this.codigoError="0";
        this.mensajeError="";
    }

	public ErrorBase(String codigoError, String mensajeError){
        this.codigoError=codigoError;
        this.mensajeError=mensajeError;
    }

	public String getCodigoError() {
		return codigoError;
	}

	public void setCodigoError(String codigoError) {
		this.codigoError = codigoError;
	}

	public String getMensajeError() {
		return mensajeError;
	}

	public void setMensajeError(String mensajeError) {
		this.mensajeError = mensajeError;
	}
		
	@Override
	public String toString() {
		return "ErrorBase [codigoError=" + codigoError + ", mensajeError=" + mensajeError + "]";
	}
	
}

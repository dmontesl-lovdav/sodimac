package com.sodimac.cfdi.util.enums;

public enum EMensajes {

	DOCUMENTO_ENVIADO_SFTP (1);
	
	EMensajes(Integer idMensaje) {
		this.idMensaje = idMensaje;
	}
	
	private Integer idMensaje;

	public Integer getIdMensaje() {
		return idMensaje;
	}

	public void setIdMensaje(Integer idMensaje) {
		this.idMensaje = idMensaje;
	}
	
	
}

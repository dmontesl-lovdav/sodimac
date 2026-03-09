package com.sodimac.cfdi.util.enums;

public enum EMensajesArchivos {

	EXTENSION_INCORRECTA			(400),
	ERROR_CARGAR_SFTP 				(401),
	DOCUMENTO_CARGADO_CORRECTAMENTE (402),
	DOCUMENTO_VACIO 				(403),
	CABECERA_INCORRECTA 			(404),
	INFORMACION_INCOMPLETA			(405),
	ERROR_CARGAR_ARCHIVO			(406);
	
	EMensajesArchivos(int id) {
		this.id = id;
	}
	
	private int id;

	public int getId() {
		return id;
	}

	public void setId(int id) {
		this.id = id;
	}
	
	
}

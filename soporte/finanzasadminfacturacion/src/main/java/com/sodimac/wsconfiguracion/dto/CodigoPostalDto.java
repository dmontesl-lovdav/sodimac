package com.sodimac.wsconfiguracion.dto;

public class CodigoPostalDto {
	
	private CodigoPostal codigoPostal;
	
	
	public CodigoPostalDto () {}
	
	public CodigoPostalDto (CodigoPostal codigoPostal) {
		this.codigoPostal = codigoPostal;
		
	}
	
	
	public CodigoPostal getCodigoPostal() {
		return codigoPostal;
	}
	public void setCodigoPostal(CodigoPostal codigoPostal) {
		this.codigoPostal = codigoPostal;
	}

}

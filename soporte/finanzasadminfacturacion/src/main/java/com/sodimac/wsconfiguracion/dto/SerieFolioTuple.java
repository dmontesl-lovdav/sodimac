package com.sodimac.wsconfiguracion.dto;

public class SerieFolioTuple {
	
	private String serie;
	private String folio;
	
	public SerieFolioTuple() {}
	
	public SerieFolioTuple(String serie, String folio) {
		this.serie = serie;
		this.folio = folio;
	}
	
	public String getSerie() {
		return serie;
	}
	public void setSerie(String serie) {
		this.serie = serie;
	}
	public String getFolio() {
		return folio;
	}
	public void setFolio(String folio) {
		this.folio = folio;
	}

}

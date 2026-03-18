package com.sodimac.wsconfiguracion.dto;

public class SerieSummaryDTO {

	private String serie;
	private Integer idcatserie;
	private Integer idconfdatosemisortienda;
	private Integer idtienda;
	private String tipocomprobante;
	
	public SerieSummaryDTO() {}
	
	public SerieSummaryDTO(String serie, Integer idcatserie, Integer idconfdatosemisortienda, Integer idtienda, String tipocomprobante) {
		this.serie = serie;
		this.idcatserie = idcatserie;
		this.idconfdatosemisortienda = idconfdatosemisortienda;
		this.idtienda = idtienda;
		this.tipocomprobante = tipocomprobante;
	}

	public String getSerie() {
		return serie;
	}

	public void setSerie(String serie) {
		this.serie = serie;
	}

	public Integer getIdcatserie() {
		return idcatserie;
	}

	public void setIdcatserie(Integer idcatserie) {
		this.idcatserie = idcatserie;
	}

	public Integer getIdconfdatosemisortienda() {
		return idconfdatosemisortienda;
	}

	public void setIdconfdatosemisortienda(Integer idconfdatosemisortienda) {
		this.idconfdatosemisortienda = idconfdatosemisortienda;
	}

	public Integer getIdtienda() {
		return idtienda;
	}

	public void setIdtienda(Integer idtienda) {
		this.idtienda = idtienda;
	}

	public String getTipocomprobante() {
		return tipocomprobante;
	}

	public void setTipocomprobante(String tipocomprobante) {
		this.tipocomprobante = tipocomprobante;
	}


	
}

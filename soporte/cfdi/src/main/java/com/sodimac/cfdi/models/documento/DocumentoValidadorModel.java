package com.sodimac.cfdi.models.documento;

public class DocumentoValidadorModel {
	private String status;
	private String message;
	private Integer totalRegistros;
	private Double peso;

	public String getStatus() {
		return status;
	}

	public void setStatus(String status) {
		this.status = status;
	}

	public String getMessage() {
		return message;
	}

	public void setMessage(String message) {
		this.message = message;
	}

	public Integer getTotalRegistros() {
		return totalRegistros;
	}

	public void setTotalRegistros(Integer totalRegistros) {
		this.totalRegistros = totalRegistros;
	}

	public Double getPeso() {
		return peso;
	}

	public void setPeso(Double peso) {
		this.peso = peso;
	}

}

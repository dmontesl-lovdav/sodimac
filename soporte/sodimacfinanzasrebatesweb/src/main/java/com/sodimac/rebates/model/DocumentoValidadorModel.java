package com.sodimac.rebates.model;

import java.util.List;

public class DocumentoValidadorModel<T> {

	private String status;
	private String message;
	private Integer totalRegistros;
	private Double peso;
	private List<T> registros;

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

	public List<T> getRegistros() {
		return registros;
	}

	public void setRegistros(List<T> registros) {
		this.registros = registros;
	}
}

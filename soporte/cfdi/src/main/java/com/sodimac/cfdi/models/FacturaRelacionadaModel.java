package com.sodimac.cfdi.models;

public class FacturaRelacionadaModel {

	private String ticket;
	private Double monto;
	private String montoStr;
	private String uuidRelacionado;
	private boolean notaCredito;

	public String getTicket() {
		return ticket;
	}

	public void setTicket(String ticket) {
		this.ticket = ticket;
	}

	public Double getMonto() {
		return monto;
	}

	public void setMonto(Double monto) {
		this.monto = monto;
	}

	public String getMontoStr() {
		return montoStr;
	}

	public void setMontoStr(String montoStr) {
		this.montoStr = montoStr;
	}

	public String getUuidRelacionado() {
		return uuidRelacionado;
	}

	public void setUuidRelacionado(String uuidRelacionado) {
		this.uuidRelacionado = uuidRelacionado;
	}

	public boolean isNotaCredito() {
		return notaCredito;
	}

	public void setNotaCredito(boolean notaCredito) {
		this.notaCredito = notaCredito;
	}

}

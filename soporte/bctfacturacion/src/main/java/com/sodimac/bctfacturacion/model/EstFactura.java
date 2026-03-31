package com.sodimac.bctfacturacion.model;

import java.util.Date;

public class EstFactura {

	private String numTrx;
	private Date fecha;
	private String numFactura;
	private String numTicket;
	private Integer codEstado;
	private String detEstado;
	private String nroSerie;
	private Integer nroFolio;
	private Integer numTienda;
	private Date fechaCierre;
	private Integer estFact;

	public String getNumTrx() {
		return numTrx;
	}

	public void setNumTrx(String numTrx) {
		this.numTrx = numTrx;
	}

	public Date getFecha() {
		return fecha;
	}

	public void setFecha(Date fecha) {
		this.fecha = fecha;
	}

	public String getNumFactura() {
		return numFactura;
	}

	public void setNumFactura(String numFactura) {
		this.numFactura = numFactura;
	}

	public String getNumTicket() {
		return numTicket;
	}

	public void setNumTicket(String numTicket) {
		this.numTicket = numTicket;
	}

	public Integer getCodEstado() {
		return codEstado;
	}

	public void setCodEstado(Integer codEstado) {
		this.codEstado = codEstado;
	}

	public String getDetEstado() {
		return detEstado;
	}

	public void setDetEstado(String detEstado) {
		this.detEstado = detEstado;
	}

	public String getNroSerie() {
		return nroSerie;
	}

	public void setNroSerie(String nroSerie) {
		this.nroSerie = nroSerie;
	}

	public Integer getNroFolio() {
		return nroFolio;
	}

	public void setNroFolio(Integer nroFolio) {
		this.nroFolio = nroFolio;
	}

	public Integer getNumTienda() {
		return numTienda;
	}

	public void setNumTienda(Integer numTienda) {
		this.numTienda = numTienda;
	}

	public Date getFechaCierre() {
		return fechaCierre;
	}

	public void setFechaCierre(Date fechaCierre) {
		this.fechaCierre = fechaCierre;
	}

	public Integer getEstFact() {
		return estFact;
	}

	public void setEstFact(Integer estFact) {
		this.estFact = estFact;
	}

}

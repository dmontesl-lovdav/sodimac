package com.sodimac.rebates.model;

import java.util.Date;

import org.springframework.format.annotation.DateTimeFormat;

public class CalculoRebateMSI {
	
	private String proveedor;
	
	private String ticket;
	
	private Integer idCatPeriodo;
	
	@DateTimeFormat(pattern = "yyyy-MM-dd")
	private Date fechaIni;
	
	@DateTimeFormat(pattern = "yyyy-MM-dd")
	private Date fechaFin;
	
	private int start;
	
	private int rowsPerPage;

	public String getProveedor() {
		return proveedor;
	}

	public void setProveedor(String proveedor) {
		this.proveedor = proveedor;
	}

	public String getTicket() {
		return ticket;
	}

	public void setTicket(String ticket) {
		this.ticket = ticket;
	}

	public Integer getIdCatPeriodo() {
		return idCatPeriodo;
	}

	public void setIdCatPeriodo(Integer idCatPeriodo) {
		this.idCatPeriodo = idCatPeriodo;
	}

	public Date getFechaIni() {
		return fechaIni;
	}

	public void setFechaIni(Date fechaIni) {
		this.fechaIni = fechaIni;
	}

	public Date getFechaFin() {
		return fechaFin;
	}

	public void setFechaFin(Date fechaFin) {
		this.fechaFin = fechaFin;
	}

	public int getStart() {
		return start;
	}

	public void setStart(int start) {
		this.start = start;
	}

	public int getRowsPerPage() {
		return rowsPerPage;
	}

	public void setRowsPerPage(int rowsPerPage) {
		this.rowsPerPage = rowsPerPage;
	}
	
}

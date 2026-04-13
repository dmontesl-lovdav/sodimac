package com.sodimac.rebates.filter;

import java.util.Date;

public class PolizaContableFilter {

	private Integer idPeriodo;
	private String idProveedor;
	private Integer tipoRebate;
	private Date fechaCargaIni;
	private Date fechaCargaFin;
	private int start;
	private int rowsPerPage;

	public Integer getIdPeriodo() {
		return idPeriodo;
	}

	public void setIdPeriodo(Integer idPeriodo) {
		this.idPeriodo = idPeriodo;
	}

	public String getIdProveedor() {
		return idProveedor;
	}

	public void setIdProveedor(String idProveedor) {
		this.idProveedor = idProveedor;
	}

	public Integer getTipoRebate() {
		return tipoRebate;
	}

	public void setTipoRebate(Integer tipoRebate) {
		this.tipoRebate = tipoRebate;
	}

	public Date getFechaCargaIni() {
		return fechaCargaIni;
	}

	public void setFechaCargaIni(Date fechaCargaIni) {
		this.fechaCargaIni = fechaCargaIni;
	}

	public Date getFechaCargaFin() {
		return fechaCargaFin;
	}

	public void setFechaCargaFin(Date fechaCargaFin) {
		this.fechaCargaFin = fechaCargaFin;
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

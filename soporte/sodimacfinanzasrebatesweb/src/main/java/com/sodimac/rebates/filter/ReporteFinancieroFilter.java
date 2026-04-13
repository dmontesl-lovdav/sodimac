package com.sodimac.rebates.filter;

import java.util.Date;

public class ReporteFinancieroFilter {

	private Integer idPeriodo;
	private Integer idProveedor;
	private String tipoPeriodo;
	private Integer tipoRebate;
	private Date fechaIni;
	private Date fechaFin;
	private int start;
	private int rowsPerPage;
	
	public ReporteFinancieroFilter(){
		
	}

	public Integer getIdPeriodo() {
		return idPeriodo;
	}

	public void setIdPeriodo(Integer idPeriodo) {
		this.idPeriodo = idPeriodo;
	}

	public Integer getIdProveedor() {
		return idProveedor;
	}

	public void setIdProveedor(Integer idProveedor) {
		this.idProveedor = idProveedor;
	}

	public String getTipoPeriodo() {
		return tipoPeriodo;
	}

	public void setTipoPeriodo(String tipoPeriodo) {
		this.tipoPeriodo = tipoPeriodo;
	}

	public Integer getTipoRebate() {
		return tipoRebate;
	}

	public void setTipoRebate(Integer tipoRebate) {
		this.tipoRebate = tipoRebate;
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

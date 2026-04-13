package com.sodimac.rebates.model;

import java.util.Date;

import org.springframework.format.annotation.DateTimeFormat;

public class ReporteOrdenCompra {
	
	private Integer proveedor;
	
	private Integer numeroOrdenCompra;
	
	private Integer tienda;
	
	private String estado;
	
	private String tipo;
	
	@DateTimeFormat(pattern = "yyyy-MM-dd")
	private Date fechaIni;
	
	@DateTimeFormat(pattern = "yyyy-MM-dd")
	private Date fechaFin;
	
	private int start;
	
	private int rowsPerPage;

	public Integer getProveedor() {
		return proveedor;
	}

	public void setProveedor(Integer proveedor) {
		this.proveedor = proveedor;
	}

	public Integer getNumeroOrdenCompra() {
		return numeroOrdenCompra;
	}

	public void setNumeroOrdenCompra(Integer numeroOrdenCompra) {
		this.numeroOrdenCompra = numeroOrdenCompra;
	}

	public Integer getTienda() {
		return tienda;
	}

	public void setTienda(Integer tienda) {
		this.tienda = tienda;
	}

	public String getEstado() {
		return estado;
	}

	public void setEstado(String estado) {
		this.estado = estado;
	}

	public String getTipo() {
		return tipo;
	}

	public void setTipo(String tipo) {
		this.tipo = tipo;
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

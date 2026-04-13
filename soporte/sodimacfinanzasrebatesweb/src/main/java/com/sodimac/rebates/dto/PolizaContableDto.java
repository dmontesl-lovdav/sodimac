package com.sodimac.rebates.dto;

import java.util.Date;

public class PolizaContableDto {

	private String id;
	private String codigoProveedor;
	private Integer idPeriodo;
	private Date fechaInicioPeriodo;
	private Date fechaFinPeriodo;
	private Integer idTipoRebate;
	private String tipoRebate;
	private Double montoCalculado;
	private Double montoPendiente;
	private Double montoContabilizado;
	private Date fechaContable;
	private Date fechaRecepcion;

	public String getId() {
		return id;
	}

	public void setId(String id) {
		this.id = id;
	}

	public String getCodigoProveedor() {
		return codigoProveedor;
	}

	public void setCodigoProveedor(String codigoProveedor) {
		this.codigoProveedor = codigoProveedor;
	}

	public Integer getIdPeriodo() {
		return idPeriodo;
	}

	public void setIdPeriodo(Integer idPeriodo) {
		this.idPeriodo = idPeriodo;
	}

	public Date getFechaInicioPeriodo() {
		return fechaInicioPeriodo;
	}

	public void setFechaInicioPeriodo(Date fechaInicioPeriodo) {
		this.fechaInicioPeriodo = fechaInicioPeriodo;
	}

	public Date getFechaFinPeriodo() {
		return fechaFinPeriodo;
	}

	public void setFechaFinPeriodo(Date fechaFinPeriodo) {
		this.fechaFinPeriodo = fechaFinPeriodo;
	}

	public Integer getIdTipoRebate() {
		return idTipoRebate;
	}

	public void setIdTipoRebate(Integer idTipoRebate) {
		this.idTipoRebate = idTipoRebate;
	}

	public String getTipoRebate() {
		return tipoRebate;
	}

	public void setTipoRebate(String tipoRebate) {
		this.tipoRebate = tipoRebate;
	}

	public Double getMontoCalculado() {
		return montoCalculado;
	}

	public void setMontoCalculado(Double montoCalculado) {
		this.montoCalculado = montoCalculado;
	}

	public Double getMontoPendiente() {
		return montoPendiente;
	}

	public void setMontoPendiente(Double montoPendiente) {
		this.montoPendiente = montoPendiente;
	}

	public Double getMontoContabilizado() {
		return montoContabilizado;
	}

	public void setMontoContabilizado(Double montoContabilizado) {
		this.montoContabilizado = montoContabilizado;
	}

	public Date getFechaContable() {
		return fechaContable;
	}

	public void setFechaContable(Date fechaContable) {
		this.fechaContable = fechaContable;
	}

	public Date getFechaRecepcion() {
		return fechaRecepcion;
	}

	public void setFechaRecepcion(Date fechaRecepcion) {
		this.fechaRecepcion = fechaRecepcion;
	}

}

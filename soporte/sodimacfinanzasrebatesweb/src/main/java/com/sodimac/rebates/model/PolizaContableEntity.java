package com.sodimac.rebates.model;

import java.io.Serializable;
import java.util.Date;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;

@Entity
@Table(name = "vw_poliza_contable")
public class PolizaContableEntity implements Serializable {
	
	private static final long serialVersionUID = 1L;

	@Id
	@Column(name = "ID", columnDefinition = "uniqueidentifier")
	private String id;

	@Column(name = "CODIGO_PROVEEDOR")
	private String codigoProveedor;
	
	@Column(name = "IdPeriodo")
	private Integer idPeriodo;
	
	@Column(name = "FECHA_INICIO_PERIODO")
	private Date fechaInicioPeriodo;
	
	@Column(name = "FECHA_FINAL_PERIODO")
	private Date fechaFinPeriodo;
	
	@Column(name = "idCatTipoRebate")
	private Integer idTipoRebate;
	
	@Column(name = "TipoRebate")
	private String tipoRebate;
	
	@Column(name = "MONTO_CALCULADO")
	private Double montoCalculado;
	
	@Column(name = "MONTO_PENDIENTE_CONTABILIZAR")
	private Double montoPendiente;
	
	@Column(name = "MONTO_CONTABILIZADO")
	private Double montoContabilizado;
	
	@Column(name = "FECHA_CONTABLE")
	private Date fechaContable;
	
	@Column(name = "FECHA_RECEPCION")
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

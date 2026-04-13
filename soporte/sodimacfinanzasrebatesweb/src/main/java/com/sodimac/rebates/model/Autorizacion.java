package com.sodimac.rebates.model;

import java.io.Serializable;
import java.math.BigDecimal;
import java.util.Date;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Table;

import org.hibernate.annotations.Immutable;
import org.springframework.format.annotation.DateTimeFormat;

@Entity
@Table(name = "AutorizacionDescuento")
@Immutable
public class Autorizacion implements Serializable {

	private static final long serialVersionUID = 1L;

	@Id
	@GeneratedValue(strategy = GenerationType.AUTO)
	@Column(name = "rowNum", updatable = false, nullable = false)
	private Integer rowNum;
	@Column
	private String iDderegistro;
	@Column
	private Integer idperiodo;
	@Column
	private Integer estatusPeriodo;
	@Column
	private String descripcionPeriodo;
	@Column
	private Integer idCatProgramaPago;
	@Column
	private String programaPago;
	@Column
	@DateTimeFormat(pattern = "yyyy-MM-dd")
	private Date fechaInicio;
	@Column
	@DateTimeFormat(pattern = "yyyy-MM-dd")
	private Date fechaFinal;
	@Column
	private String cuenta;
	@Column
	private BigDecimal importe;
	@Column
	private BigDecimal importeAnterior;
	@Column
	private Integer tipodeRebate;
	@Column
	private String tipoRebate;
	@Column
	private char estatus;
	@Column
	private String descripcionEstatus;

	public Integer getRowNum() {
		return rowNum;
	}

	public void setRowNum(Integer rowNum) {
		this.rowNum = rowNum;
	}

	public String getiDderegistro() {
		return iDderegistro;
	}

	public void setiDderegistro(String iDderegistro) {
		this.iDderegistro = iDderegistro;
	}

	public Integer getIdperiodo() {
		return idperiodo;
	}

	public void setIdperiodo(Integer idperiodo) {
		this.idperiodo = idperiodo;
	}

	public Integer getEstatusPeriodo() {
		return estatusPeriodo;
	}

	public void setEstatusPeriodo(Integer estatusPeriodo) {
		this.estatusPeriodo = estatusPeriodo;
	}

	public String getDescripcionPeriodo() {
		return descripcionPeriodo;
	}

	public void setDescripcionPeriodo(String descripcionPeriodo) {
		this.descripcionPeriodo = descripcionPeriodo;
	}

	public Integer getIdCatProgramaPago() {
		return idCatProgramaPago;
	}

	public void setIdCatProgramaPago(Integer idCatProgramaPago) {
		this.idCatProgramaPago = idCatProgramaPago;
	}

	public String getProgramaPago() {
		return programaPago;
	}

	public void setProgramaPago(String programaPago) {
		this.programaPago = programaPago;
	}

	public Date getFechaInicio() {
		return fechaInicio;
	}

	public void setFechaInicio(Date fechaInicio) {
		this.fechaInicio = fechaInicio;
	}

	public Date getFechaFinal() {
		return fechaFinal;
	}

	public void setFechaFinal(Date fechaFinal) {
		this.fechaFinal = fechaFinal;
	}

	public String getCuenta() {
		return cuenta;
	}

	public void setCuenta(String cuenta) {
		this.cuenta = cuenta;
	}

	public BigDecimal getImporte() {
		return importe;
	}

	public void setImporte(BigDecimal importe) {
		this.importe = importe;
	}

	public BigDecimal getImporteAnterior() {
		return importeAnterior;
	}

	public void setImporteAnterior(BigDecimal importeAnterior) {
		this.importeAnterior = importeAnterior;
	}

	public Integer getTipodeRebate() {
		return tipodeRebate;
	}

	public void setTipodeRebate(Integer tipodeRebate) {
		this.tipodeRebate = tipodeRebate;
	}

	public String getTipoRebate() {
		return tipoRebate;
	}

	public void setTipoRebate(String tipoRebate) {
		this.tipoRebate = tipoRebate;
	}

	public char getEstatus() {
		return estatus;
	}

	public void setEstatus(char estatus) {
		this.estatus = estatus;
	}

	public String getDescripcionEstatus() {
		return descripcionEstatus;
	}

	public void setDescripcionEstatus(String descripcionEstatus) {
		this.descripcionEstatus = descripcionEstatus;
	}

	public static long getSerialversionuid() {
		return serialVersionUID;
	}

	@Override
	public String toString() {
		return "Autorizacion [rowNum=" + rowNum + ", iDderegistro=" + iDderegistro + ", idperiodo=" + idperiodo
				+ ", estatusPeriodo=" + estatusPeriodo + ", descripcionPeriodo=" + descripcionPeriodo
				+ ", idCatProgramaPago=" + idCatProgramaPago + ", programaPago=" + programaPago + ", fechaInicio="
				+ fechaInicio + ", fechaFinal=" + fechaFinal + ", cuenta=" + cuenta + ", importe=" + importe
				+ ", importeAnterior=" + importeAnterior + ", tipodeRebate=" + tipodeRebate + ", tipoRebate="
				+ tipoRebate + ", estatus=" + estatus + ", descripcionEstatus=" + descripcionEstatus + "]";
	}

}

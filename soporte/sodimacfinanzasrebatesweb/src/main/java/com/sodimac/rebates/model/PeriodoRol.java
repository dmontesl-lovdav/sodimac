package com.sodimac.rebates.model;

import java.util.Date;

import javax.persistence.*;

import org.springframework.format.annotation.DateTimeFormat;

@Entity
@Table(name = "vw_perfilPeriodoRebate")
public class PeriodoRol {

	@Id
	private Integer idCatPeriodo;
	private String detallePeriodo;
	@OneToOne
	@JoinColumn(name = "idCatProgramaPago")
	private ProgramaPago programaPago;	
	@DateTimeFormat(pattern = "yyyy-MM-dd")
	private Date fechaIni;
	private Integer estatus;
	private Integer idCatPerfil;
	private boolean activo;
	private int orden;
	
	public Integer getIdCatPeriodo() {
		return idCatPeriodo;
	}
	public void setIdCatPeriodo(Integer idCatPeriodo) {
		this.idCatPeriodo = idCatPeriodo;
	}
	public String getDetallePeriodo() {
		return detallePeriodo;
	}
	public void setDetallePeriodo(String detallePeriodo) {
		this.detallePeriodo = detallePeriodo;
	}
	public ProgramaPago getProgramaPago() {
		return programaPago;
	}
	public void setProgramaPago(ProgramaPago programaPago) {
		this.programaPago = programaPago;
	}
	public Date getFechaIni() {
		return fechaIni;
	}
	public void setFechaIni(Date fechaIni) {
		this.fechaIni = fechaIni;
	}
	public Integer getEstatus() {
		return estatus;
	}
	public void setEstatus(Integer estatus) {
		this.estatus = estatus;
	}
	public Integer getIdCatPerfil() {
		return idCatPerfil;
	}
	public void setIdCatPerfil(Integer idCatPerfil) {
		this.idCatPerfil = idCatPerfil;
	}
	public boolean isActivo() {
		return activo;
	}
	public void setActivo(boolean activo) {
		this.activo = activo;
	}
	public int getOrden() {
		return orden;
	}
	public void setOrden(int orden) {
		this.orden = orden;
	}
	
	@Override
	public String toString() {
		return "PeriodoRol [idCatPeriodo=" + idCatPeriodo + ", detallePeriodo=" + detallePeriodo + ", programaPago="
				+ programaPago + ", fechaIni=" + fechaIni + ", estatus=" + estatus + ", idCatPerfil=" + idCatPerfil
				+ ", activo=" + activo + ", orden=" + orden + "]";
	}
	
}

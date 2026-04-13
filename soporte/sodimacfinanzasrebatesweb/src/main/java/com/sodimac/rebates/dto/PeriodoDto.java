package com.sodimac.rebates.dto;

import java.util.Date;
import java.util.List;

import org.springframework.format.annotation.DateTimeFormat;

public class PeriodoDto {

	private Integer idCatPeriodo;
	private String detallePeriodo;
	private Date fechaIni;
	private Date fechaFin;
	private Date fechaEnvio;
	private Date fechaCalculo;
	private Integer estatus;
	private boolean activo;
	private int orden;
	private ProgramaPagoDto programaPago;
	private List<RelPeriodoTipoRebateDto> relPeriodoTipoRebate;
	private String relJsonString;
	private List<RelacionPeriodoRebate> tiposRebateRelacion;
	private CatFlujoEstatusDto flujoEstatus;
	private boolean procesar;
	private boolean reprocesar;

	private Integer idUsuarioCreacion;
	private Date fechaHoraCreacion;
	private Integer idUsuarioModificacion;
	private Date fechaHoraModificacion;
	private Integer idUsuarioModifEstatus;
	private Date fechaHoraModifEstatus;
	private String nombreUsuarioCreacion;
	private String nombreUsuarioModificacion;
	private String nombreUsuarioModifEstatus;
	private Integer idPerfil;
	private Date fechaHoraCierre;
	private Date fechaHoraRespaldo;

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

	public Date getFechaEnvio() {
		return fechaEnvio;
	}

	public void setFechaEnvio(Date fechaEnvio) {
		this.fechaEnvio = fechaEnvio;
	}

	public Integer getEstatus() {
		return estatus;
	}

	public void setEstatus(Integer estatus) {
		this.estatus = estatus;
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

	public ProgramaPagoDto getProgramaPago() {
		return programaPago;
	}

	public void setProgramaPago(ProgramaPagoDto programaPago) {
		this.programaPago = programaPago;
	}

	public List<RelPeriodoTipoRebateDto> getRelPeriodoTipoRebate() {
		return relPeriodoTipoRebate;
	}

	public void setRelPeriodoTipoRebate(List<RelPeriodoTipoRebateDto> relPeriodoTipoRebate) {
		this.relPeriodoTipoRebate = relPeriodoTipoRebate;
	}
	
	public String getRelJsonString() {
		return relJsonString;
	}

	public void setRelJsonString(String relJsonString) {
		this.relJsonString = relJsonString;
	}
	
	public List<RelacionPeriodoRebate> getTiposRebateRelacion() {
		return tiposRebateRelacion;
	}

	public void setTiposRebateRelacion(List<RelacionPeriodoRebate> tiposRebateRelacion) {
		this.tiposRebateRelacion = tiposRebateRelacion;
	}

	public CatFlujoEstatusDto getFlujoEstatus() {
		return flujoEstatus;
	}

	public void setFlujoEstatus(CatFlujoEstatusDto flujoEstatus) {
		this.flujoEstatus = flujoEstatus;
	}

	public boolean isProcesar() {
		return procesar;
	}

	public void setProcesar(boolean procesar) {
		this.procesar = procesar;
	}

	public boolean isReprocesar() {
		return reprocesar;
	}

	public void setReprocesar(boolean reprocesar) {
		this.reprocesar = reprocesar;
	}

	public Date getFechaCalculo() {
		return fechaCalculo;
	}

	public void setFechaCalculo(Date fechaCalculo) {
		this.fechaCalculo = fechaCalculo;
	}

	public Integer getIdUsuarioCreacion() {
		return idUsuarioCreacion;
	}

	public void setIdUsuarioCreacion(Integer idUsuarioCreacion) {
		this.idUsuarioCreacion = idUsuarioCreacion;
	}

	public Date getFechaHoraCreacion() {
		return fechaHoraCreacion;
	}

	public void setFechaHoraCreacion(Date fechaHoraCreacion) {
		this.fechaHoraCreacion = fechaHoraCreacion;
	}

	public Integer getIdUsuarioModificacion() {
		return idUsuarioModificacion;
	}

	public void setIdUsuarioModificacion(Integer idUsuarioModificacion) {
		this.idUsuarioModificacion = idUsuarioModificacion;
	}

	public Date getFechaHoraModificacion() {
		return fechaHoraModificacion;
	}

	public void setFechaHoraModificacion(Date fechaHoraModificacion) {
		this.fechaHoraModificacion = fechaHoraModificacion;
	}

	public Integer getIdUsuarioModifEstatus() {
		return idUsuarioModifEstatus;
	}

	public void setIdUsuarioModifEstatus(Integer idUsuarioModifEstatus) {
		this.idUsuarioModifEstatus = idUsuarioModifEstatus;
	}

	public Date getFechaHoraModifEstatus() {
		return fechaHoraModifEstatus;
	}

	public void setFechaHoraModifEstatus(Date fechaHoraModifEstatus) {
		this.fechaHoraModifEstatus = fechaHoraModifEstatus;
	}

	public String getNombreUsuarioCreacion() {
		return nombreUsuarioCreacion;
	}

	public void setNombreUsuarioCreacion(String nombreUsuarioCreacion) {
		this.nombreUsuarioCreacion = nombreUsuarioCreacion;
	}

	public String getNombreUsuarioModificacion() {
		return nombreUsuarioModificacion;
	}

	public void setNombreUsuarioModificacion(String nombreUsuarioModificacion) {
		this.nombreUsuarioModificacion = nombreUsuarioModificacion;
	}

	public String getNombreUsuarioModifEstatus() {
		return nombreUsuarioModifEstatus;
	}

	public void setNombreUsuarioModifEstatus(String nombreUsuarioModifEstatus) {
		this.nombreUsuarioModifEstatus = nombreUsuarioModifEstatus;
	}

	public Integer getIdPerfil() {
		return idPerfil;
	}

	public void setIdPerfil(Integer idPerfil) {
		this.idPerfil = idPerfil;
	}

	public Date getFechaHoraCierre() {
		return fechaHoraCierre;
	}

	public void setFechaHoraCierre(Date fechaHoraCierre) {
		this.fechaHoraCierre = fechaHoraCierre;
	}

	public Date getFechaHoraRespaldo() {
		return fechaHoraRespaldo;
	}

	public void setFechaHoraRespaldo(Date fechaHoraRespaldo) {
		this.fechaHoraRespaldo = fechaHoraRespaldo;
	}

	@Override
	public String toString() {
		return "PeriodoDto [idCatPeriodo=" + idCatPeriodo + ", detallePeriodo=" + detallePeriodo + ", fechaIni="
				+ fechaIni + ", fechaFin=" + fechaFin + ", fechaEnvio=" + fechaEnvio + ", fechaCalculo=" + fechaCalculo
				+ ", estatus=" + estatus + ", activo=" + activo + ", orden=" + orden + ", programaPago=" + programaPago
				+ ", relPeriodoTipoRebate=" + relPeriodoTipoRebate + ", relJsonString=" + relJsonString
				+ ", tiposRebateRelacion=" + tiposRebateRelacion + ", flujoEstatus=" + flujoEstatus + ", procesar="
				+ procesar + ", reprocesar=" + reprocesar + ", idUsuarioCreacion=" + idUsuarioCreacion
				+ ", fechaHoraCreacion=" + fechaHoraCreacion + ", idUsuarioModificacion=" + idUsuarioModificacion
				+ ", fechaHoraModificacion=" + fechaHoraModificacion + ", idUsuarioModifEstatus="
				+ idUsuarioModifEstatus + ", fechaHoraModifEstatus=" + fechaHoraModifEstatus
				+ ", nombreUsuarioCreacion=" + nombreUsuarioCreacion + ", nombreUsuarioModificacion="
				+ nombreUsuarioModificacion + ", nombreUsuarioModifEstatus=" + nombreUsuarioModifEstatus + ", idPerfil="
				+ idPerfil + ", fechaHoraCierre=" + fechaHoraCierre + ", fechaHoraRespaldo=" + fechaHoraRespaldo + "]";
	}

}

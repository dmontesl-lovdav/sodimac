package com.sodimac.rebates.model;

import java.util.Date;
import java.util.List;

import javax.persistence.*;

import org.hibernate.annotations.CreationTimestamp;
import org.springframework.format.annotation.DateTimeFormat;

import com.fasterxml.jackson.annotation.JsonBackReference;

@Entity
@Table(name = "CatPeriodo")
public class Periodo {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Integer idCatPeriodo;
	private String detallePeriodo;
	@OneToOne
	@JoinColumn(name = "idCatProgramaPago")
	private ProgramaPago programaPago;
	@DateTimeFormat(pattern = "yyyy-MM-dd")
	private Date fechaIni;
	@DateTimeFormat(pattern = "yyyy-MM-dd")
	private Date fechaFin;
	@DateTimeFormat(pattern = "yyyy-MM-dd")
	private Date fechaEnvio;
	@DateTimeFormat(pattern = "yyyy-MM-dd hh:mm:ss")
	private Date fechaCalculo;
	private Integer estatus;
	private boolean activo;
	@JsonBackReference(value = "periodo")
	@OneToMany(mappedBy = "periodo")
	private List<RelPeriodoTipoRebate> relPeriodoTipoRebate;
	@Column(name = "Orden")
	private int orden;
	private Integer idUsuarioCreacion;
    @CreationTimestamp
	@Temporal(TemporalType.TIMESTAMP)
	@DateTimeFormat(pattern = "yyyy-MM-dd hh:mm:ss")
	private Date fechaHoraCreacion;
	private Integer idUsuarioModificacion;
	@DateTimeFormat(pattern = "yyyy-MM-dd hh:mm:ss")
	private Date fechaHoraModificacion;
	private Integer idUsuarioModifEstatus;
	@DateTimeFormat(pattern = "yyyy-MM-dd hh:mm:ss")
	private Date fechaHoraModifEstatus;
	@Column(name = "IdPerfil")
	private Integer idPerfil;
	@DateTimeFormat(pattern = "yyyy-MM-dd hh:mm:ss")
	private Date fechaHoraCierre;
	@DateTimeFormat(pattern = "yyyy-MM-dd hh:mm:ss")
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
	
	public List<RelPeriodoTipoRebate> getRelPeriodoTipoRebate() {
		return relPeriodoTipoRebate;
	}

	public void setRelPeriodoTipoRebate(List<RelPeriodoTipoRebate> relPeriodoTipoRebate) {
		this.relPeriodoTipoRebate = relPeriodoTipoRebate;
	}

	public int getOrden() {
		return orden;
	}

	public void setOrden(int orden) {
		this.orden = orden;
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
		return "Periodo [idCatPeriodo=" + idCatPeriodo + ", detallePeriodo=" + detallePeriodo + ", programaPago="
				+ programaPago + ", fechaIni=" + fechaIni + ", fechaFin=" + fechaFin + ", fechaEnvio=" + fechaEnvio
				+ ", fechaCalculo=" + fechaCalculo + ", estatus=" + estatus + ", activo=" + activo
				+ ", relPeriodoTipoRebate=" + relPeriodoTipoRebate + ", orden=" + orden + ", idUsuarioCreacion="
				+ idUsuarioCreacion + ", fechaHoraCreacion=" + fechaHoraCreacion + ", idUsuarioModificacion="
				+ idUsuarioModificacion + ", fechaHoraModificacion=" + fechaHoraModificacion
				+ ", idUsuarioModifEstatus=" + idUsuarioModifEstatus + ", fechaHoraModifEstatus="
				+ fechaHoraModifEstatus + ", idPerfil=" + idPerfil + ", fechaHoraCierre=" + fechaHoraCierre
				+ ", fechaHoraRespaldo=" + fechaHoraRespaldo + "]";
	}

}

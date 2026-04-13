package com.sodimac.rebates.model;

import java.util.Date;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.OneToOne;
import javax.persistence.Table;
import javax.persistence.Transient;

@Entity
@Table(name = "controlDocumento")
public class ControlDocumento extends Generic {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Integer idCarga;
	@OneToOne
	@JoinColumn(name = "idDocumento")
	private Documento documento;
	private String nombreArchivo;
	private String nombreArchivoProcesado;
	@OneToOne
	@JoinColumn(name = "idPeriodo", referencedColumnName = "idCatPeriodo")
	private Periodo periodo;
	private Integer peso;
	private short idEstatusArchivo;
	private Integer usuario;
	private Date fechaHoraCarga;
	private Date fechaHoraProceso;
	private String rutaDocumento;
	private boolean activo;
	@Transient
    private boolean eliminar;
	@Transient
    private String nombreUsuario;

	public Integer getIdCarga() {
		return idCarga;
	}

	public void setIdCarga(Integer idCarga) {
		this.idCarga = idCarga;
	}

	public Documento getDocumento() {
		return documento;
	}

	public void setDocumento(Documento documento) {
		this.documento = documento;
	}

	public String getNombreArchivo() {
		return nombreArchivo;
	}

	public void setNombreArchivo(String nombreArchivo) {
		this.nombreArchivo = nombreArchivo;
	}

	public String getNombreArchivoProcesado() {
		return nombreArchivoProcesado;
	}

	public void setNombreArchivoProcesado(String nombreArchivoProcesado) {
		this.nombreArchivoProcesado = nombreArchivoProcesado;
	}

	public Periodo getPeriodo() {
		return periodo;
	}

	public void setPeriodo(Periodo periodo) {
		this.periodo = periodo;
	}

	public Integer getPeso() {
		return peso;
	}

	public void setPeso(Integer peso) {
		this.peso = peso;
	}

	public short getIdEstatusArchivo() {
		return idEstatusArchivo;
	}

	public void setIdEstatusArchivo(short idEstatusArchivo) {
		this.idEstatusArchivo = idEstatusArchivo;
	}

	public Integer getUsuario() {
		return usuario;
	}

	public void setUsuario(Integer usuario) {
		this.usuario = usuario;
	}

	public Date getFechaHoraCarga() {
		return fechaHoraCarga;
	}

	public void setFechaHoraCarga(Date fechaHoraCarga) {
		this.fechaHoraCarga = fechaHoraCarga;
	}

	public Date getFechaHoraProceso() {
		return fechaHoraProceso;
	}

	public void setFechaHoraProceso(Date fechaHoraProceso) {
		this.fechaHoraProceso = fechaHoraProceso;
	}

	public String getRutaDocumento() {
		return rutaDocumento;
	}

	public void setRutaDocumento(String rutaDocumento) {
		this.rutaDocumento = rutaDocumento;
	}

	public boolean isActivo() {
		return activo;
	}

	public void setActivo(boolean activo) {
		this.activo = activo;
	}

	public boolean isEliminar() {
		return eliminar;
	}

	public void setEliminar(boolean eliminar) {
		this.eliminar = eliminar;
	}

	public String getNombreUsuario() {
		return nombreUsuario;
	}

	public void setNombreUsuario(String nombreUsuario) {
		this.nombreUsuario = nombreUsuario;
	}

	@Override
	public String toString() {
		return "ControlDocumento [idCarga=" + idCarga + ", documento=" + documento + ", nombreArchivo=" + nombreArchivo
				+ ", nombreArchivoProcesado=" + nombreArchivoProcesado + ", periodo=" + periodo + ", peso=" + peso
				+ ", idEstatusArchivo=" + idEstatusArchivo + ", usuario=" + usuario + ", fechaHoraCarga="
				+ fechaHoraCarga + ", fechaHoraProceso=" + fechaHoraProceso + ", rutaDocumento=" + rutaDocumento
				+ ", activo=" + activo + "]";
	}

}

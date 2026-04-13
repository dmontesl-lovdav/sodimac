package com.sodimac.rebates.dto;

import java.util.Date;

public class ExclusionCargaDto {

	private String jsonId;
	private Long idExclusionCarga;
	private Integer idExclusion;
	private String carga;
	private String motivo;
	private Date fechaRegistro;
	private boolean activo;
	private String numProveedor;
	private String nomProveedor;
	private Integer periodoVigente;
	private Date fechaRecepcion;
	private boolean tieneAcuerdo;

	public String getJsonId() {
		return jsonId;
	}

	public void setJsonId(String jsonId) {
		this.jsonId = jsonId;
	}

	public Long getIdExclusionCarga() {
		return idExclusionCarga;
	}

	public void setIdExclusionCarga(Long idExclusionCarga) {
		this.idExclusionCarga = idExclusionCarga;
	}

	public Integer getIdExclusion() {
		return idExclusion;
	}

	public void setIdExclusion(Integer idExclusion) {
		this.idExclusion = idExclusion;
	}
	
	public String getCarga() {
		return carga;
	}

	public void setCarga(String carga) {
		this.carga = carga;
	}

	public String getMotivo() {
		return motivo;
	}

	public void setMotivo(String motivo) {
		this.motivo = motivo;
	}

	public Date getFechaRegistro() {
		return fechaRegistro;
	}

	public void setFechaRegistro(Date fechaRegistro) {
		this.fechaRegistro = fechaRegistro;
	}

	public boolean isActivo() {
		return activo;
	}

	public void setActivo(boolean activo) {
		this.activo = activo;
	}

	public String getNumProveedor() {
		return numProveedor;
	}

	public void setNumProveedor(String numProveedor) {
		this.numProveedor = numProveedor;
	}

	public String getNomProveedor() {
		return nomProveedor;
	}

	public void setNomProveedor(String nomProveedor) {
		this.nomProveedor = nomProveedor;
	}

	public Integer getPeriodoVigente() {
		return periodoVigente;
	}

	public void setPeriodoVigente(Integer periodoVigente) {
		this.periodoVigente = periodoVigente;
	}

	public Date getFechaRecepcion() {
		return fechaRecepcion;
	}

	public void setFechaRecepcion(Date fechaRecepcion) {
		this.fechaRecepcion = fechaRecepcion;
	}

	public boolean isTieneAcuerdo() {
		return tieneAcuerdo;
	}

	public void setTieneAcuerdo(boolean tieneAcuerdo) {
		this.tieneAcuerdo = tieneAcuerdo;
	}

}

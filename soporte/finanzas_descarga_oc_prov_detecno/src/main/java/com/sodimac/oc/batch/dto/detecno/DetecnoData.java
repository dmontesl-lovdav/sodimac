package com.sodimac.oc.batch.dto.detecno;

import com.fasterxml.jackson.annotation.JsonProperty;

public class DetecnoData {

	@JsonProperty(value = "Id")
	private Integer id;
	@JsonProperty(value = "CodProvSAP")
	private String codProvSAP;
	@JsonProperty(value = "NoOC")
	private String noOC;
	@JsonProperty(value = "NoRecepcion")
	private String noRecepcion;
	@JsonProperty(value = "Sucursal")
	private String sucursal;
	@JsonProperty(value = "NoGuia")
	private String noGuia;
	@JsonProperty(value = "ImporteSinImpuestos")
	private Double importeSinImpuestos;
	@JsonProperty(value = "FechaRecepcion")
	private String fechaRecepcion;
	@JsonProperty(value = "Estatus")
	private String estatus;
	@JsonProperty(value = "Origen")
	private String origen;
	@JsonProperty(value = "FechaMovimiento")
	private String fechaMovimiento;
	@JsonProperty(value = "MotivoCancelacion")
	private String motivoCancelacion;

	public DetecnoData() {

	}

	public Integer getId() {
		return id;
	}

	public void setId(Integer id) {
		this.id = id;
	}

	public String getCodProvSAP() {
		return codProvSAP;
	}

	public void setCodProvSAP(String codProvSAP) {
		this.codProvSAP = codProvSAP;
	}

	public String getNoOC() {
		return noOC;
	}

	public void setNoOC(String noOC) {
		this.noOC = noOC;
	}

	public String getNoRecepcion() {
		return noRecepcion;
	}

	public void setNoRecepcion(String noRecepcion) {
		this.noRecepcion = noRecepcion;
	}

	public String getSucursal() {
		return sucursal;
	}

	public void setSucursal(String sucursal) {
		this.sucursal = sucursal;
	}

	public String getNoGuia() {
		return noGuia;
	}

	public void setNoGuia(String noGuia) {
		this.noGuia = noGuia;
	}

	public Double getImporteSinImpuestos() {
		return importeSinImpuestos;
	}

	public void setImporteSinImpuestos(Double importeSinImpuestos) {
		this.importeSinImpuestos = importeSinImpuestos;
	}

	public String getFechaRecepcion() {
		return fechaRecepcion;
	}

	public void setFechaRecepcion(String fechaRecepcion) {
		this.fechaRecepcion = fechaRecepcion;
	}

	public String getEstatus() {
		return estatus;
	}

	public void setEstatus(String estatus) {
		this.estatus = estatus;
	}

	public String getOrigen() {
		return origen;
	}

	public void setOrigen(String origen) {
		this.origen = origen;
	}

	public String getFechaMovimiento() {
		return fechaMovimiento;
	}

	public void setFechaMovimiento(String fechaMovimiento) {
		this.fechaMovimiento = fechaMovimiento;
	}

	public String getMotivoCancelacion() {
		return motivoCancelacion;
	}

	public void setMotivoCancelacion(String motivoCancelacion) {
		this.motivoCancelacion = motivoCancelacion;
	}

}

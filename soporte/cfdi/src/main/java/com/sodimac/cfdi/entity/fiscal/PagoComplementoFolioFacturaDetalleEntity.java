package com.sodimac.cfdi.entity.fiscal;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Table;

@Entity
@Table(name = "pagocomplementofoliofactura")
public class PagoComplementoFolioFacturaDetalleEntity {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	@Column(name = "idPagoComplementoFolioFacturaDet")
	private Integer idPagoComplementoFolioFacturaDet;
	
	@Column(name = "idPagoComplementoFolioFactura")
	private Integer idPagoComplementoFolioFactura;
	
	@Column(name = "idFactura")
	private Integer idFactura;
	
	@Column(name = "parcialidad")
	private Integer parcialidad;
	
	@Column(name = "equivalencia")
	private Double equivalencia;
	
	@Column(name = "monedaPago")
	private String monedaPago;
	
	@Column(name = "montoFactura")
	private Double montoFactura;

	@Column(name = "importeSaldoAnterior")
	private Double importeSaldoAnterior;

	@Column(name = "importePosterior")
	private Double importePosterior;

	@Column(name = "estatusFactura")
	private Integer estatusFactura;

	public Integer getIdPagoComplementoFolioFacturaDet() {
		return idPagoComplementoFolioFacturaDet;
	}

	public void setIdPagoComplementoFolioFacturaDet(Integer idPagoComplementoFolioFacturaDet) {
		this.idPagoComplementoFolioFacturaDet = idPagoComplementoFolioFacturaDet;
	}

	public Integer getIdPagoComplementoFolioFactura() {
		return idPagoComplementoFolioFactura;
	}

	public void setIdPagoComplementoFolioFactura(Integer idPagoComplementoFolioFactura) {
		this.idPagoComplementoFolioFactura = idPagoComplementoFolioFactura;
	}

	public Integer getIdFactura() {
		return idFactura;
	}

	public void setIdFactura(Integer idFactura) {
		this.idFactura = idFactura;
	}

	public Integer getParcialidad() {
		return parcialidad;
	}

	public void setParcialidad(Integer parcialidad) {
		this.parcialidad = parcialidad;
	}

	public Double getEquivalencia() {
		return equivalencia;
	}

	public void setEquivalencia(Double equivalencia) {
		this.equivalencia = equivalencia;
	}

	public String getMonedaPago() {
		return monedaPago;
	}

	public void setMonedaPago(String monedaPago) {
		this.monedaPago = monedaPago;
	}

	public Double getMontoFactura() {
		return montoFactura;
	}

	public void setMontoFactura(Double montoFactura) {
		this.montoFactura = montoFactura;
	}

	public Double getImporteSaldoAnterior() {
		return importeSaldoAnterior;
	}

	public void setImporteSaldoAnterior(Double importeSaldoAnterior) {
		this.importeSaldoAnterior = importeSaldoAnterior;
	}

	public Double getImportePosterior() {
		return importePosterior;
	}

	public void setImportePosterior(Double importePosterior) {
		this.importePosterior = importePosterior;
	}

	public Integer getEstatusFactura() {
		return estatusFactura;
	}

	public void setEstatusFactura(Integer estatusFactura) {
		this.estatusFactura = estatusFactura;
	}

}

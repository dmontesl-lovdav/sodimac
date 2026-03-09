package com.sodimac.cfdi.models;

import java.util.List;

import com.sodimac.cfdi.util.enums.EFacturaComplemento;

public class PagoComplementoFolioFacturaDetalleModel {

	private Integer idPagoComplementoFolioFacturaDet;
	private Integer idPagoComplementoFolioFactura;
	private Integer idFactura;
	private Integer parcialidad;
	private Double equivalencia;
	private String monedaPago;
	private Double montoRealFactura;
	private Double montoTotalNC;
	private Double montoPorPagar;
	private Double importeSaldoAnterior;
	private Double importePagado;
	private Double importePosterior;
	private EFacturaComplemento estatusFactura;
	private String uuid;
	private String rfc;
	private String razonSocial;
	private boolean persistente;
	private boolean colorPagoComplemento;
	private Integer orden;
	private PagoComplementoFolioFacturaModel pagoComplementoFolioFactura; 
	
	private List<PagoComplementoFolioFacturaDetalleModel> facturasPagosRelacionados;

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

	public Double getMontoRealFactura() {
		return montoRealFactura;
	}

	public void setMontoRealFactura(Double montoRealFactura) {
		this.montoRealFactura = montoRealFactura;
	}

	public Double getMontoTotalNC() {
		return montoTotalNC;
	}

	public void setMontoTotalNC(Double montoTotalNC) {
		this.montoTotalNC = montoTotalNC;
	}
	
	public Double getMontoPorPagar() {
		return montoPorPagar;
	}

	public void setMontoPorPagar(Double montoPorPagar) {
		this.montoPorPagar = montoPorPagar;
	}

	public Double getImporteSaldoAnterior() {
		return importeSaldoAnterior;
	}

	public void setImporteSaldoAnterior(Double importeSaldoAnterior) {
		this.importeSaldoAnterior = importeSaldoAnterior;
	}

	public Double getImportePagado() {
		return importePagado;
	}

	public void setImportePagado(Double importePagado) {
		this.importePagado = importePagado;
	}

	public Double getImportePosterior() {
		return importePosterior;
	}

	public void setImportePosterior(Double importePosterior) {
		this.importePosterior = importePosterior;
	}

	public EFacturaComplemento getEstatusFactura() {
		return estatusFactura;
	}

	public void setEstatusFactura(EFacturaComplemento estatusFactura) {
		this.estatusFactura = estatusFactura;
	}
	
	public String getUuid() {
		return uuid;
	}

	public void setUuid(String uuid) {
		this.uuid = uuid;
	}

	public String getRfc() {
		return rfc;
	}

	public void setRfc(String rfc) {
		this.rfc = rfc;
	}

	public String getRazonSocial() {
		return razonSocial;
	}

	public void setRazonSocial(String razonSocial) {
		this.razonSocial = razonSocial;
	}
	
	public boolean isPersistente() {
		return persistente;
	}

	public void setPersistente(boolean persistente) {
		this.persistente = persistente;
	}

	public boolean isColorPagoComplemento() {
		return colorPagoComplemento;
	}

	public void setColorPagoComplemento(boolean colorPagoComplemento) {
		this.colorPagoComplemento = colorPagoComplemento;
	}

	public PagoComplementoFolioFacturaModel getPagoComplementoFolioFactura() {
		return pagoComplementoFolioFactura;
	}

	public void setPagoComplementoFolioFactura(PagoComplementoFolioFacturaModel pagoComplementoFolioFactura) {
		this.pagoComplementoFolioFactura = pagoComplementoFolioFactura;
	}

	public List<PagoComplementoFolioFacturaDetalleModel> getFacturasPagosRelacionados() {
		return facturasPagosRelacionados;
	}

	public void setFacturasPagosRelacionados(List<PagoComplementoFolioFacturaDetalleModel> facturasPagosRelacionados) {
		this.facturasPagosRelacionados = facturasPagosRelacionados;
	}

	public Integer getOrden() {
		return orden;
	}

	public void setOrden(Integer orden) {
		this.orden = orden;
	}

}

package com.sodimac.cfdi.models;

import java.util.List;

public class PagoComplementoFolioFacturaModel {

	private Integer idPagoComplementoFolioFactura;
	private Integer idPagoComplemento;
	private Integer idFolioFactura;
	private String folioFactura;
	private Double totalFolioFactura;
	private Double montoPago;
	private boolean persistente;
	private Integer orden;
	private List<PagoComplementoFolioFacturaDetalleModel> pagosComplementoFolioFacturaDet;

	public Integer getIdPagoComplementoFolioFactura() {
		return idPagoComplementoFolioFactura;
	}

	public void setIdPagoComplementoFolioFactura(Integer idPagoComplementoFolioFactura) {
		this.idPagoComplementoFolioFactura = idPagoComplementoFolioFactura;
	}

	public Integer getIdPagoComplemento() {
		return idPagoComplemento;
	}

	public void setIdPagoComplemento(Integer idPagoComplemento) {
		this.idPagoComplemento = idPagoComplemento;
	}

	public Integer getIdFolioFactura() {
		return idFolioFactura;
	}

	public void setIdFolioFactura(Integer idFolioFactura) {
		this.idFolioFactura = idFolioFactura;
	}

	public String getFolioFactura() {
		return folioFactura;
	}

	public void setFolioFactura(String folioFactura) {
		this.folioFactura = folioFactura;
	}

	public Double getTotalFolioFactura() {
		return totalFolioFactura;
	}

	public void setTotalFolioFactura(Double totalFolioFactura) {
		this.totalFolioFactura = totalFolioFactura;
	}

	public Double getMontoPago() {
		return montoPago;
	}

	public void setMontoPago(Double montoPago) {
		this.montoPago = montoPago;
	}

	public boolean isPersistente() {
		return persistente;
	}

	public void setPersistente(boolean persistente) {
		this.persistente = persistente;
	}
	
	public Integer getOrden() {
		return orden;
	}

	public void setOrden(Integer orden) {
		this.orden = orden;
	}

	public List<PagoComplementoFolioFacturaDetalleModel> getPagosComplementoFolioFacturaDet() {
		return pagosComplementoFolioFacturaDet;
	}

	public void setPagosComplementoFolioFacturaDet(
			List<PagoComplementoFolioFacturaDetalleModel> pagosComplementoFolioFacturaDet) {
		this.pagosComplementoFolioFacturaDet = pagosComplementoFolioFacturaDet;
	}

}

package com.sodimac.cfdi.models;

import java.util.List;

public class PagoComplementoDetalleModel {
	private String rfc;
	private String razonSocial;
	private Double importePagoComplemento;
	private String importePagoComplementoStr;
	private Double totalFacturas;
	private String totalFacturasStr;
	private Double totalComplemento;
	private String totalComplementoStr;
	private String folioFactura;
	private boolean existePagoComplemento;
	private String msgValidacion;
	
	private boolean btnVincularPago;
	private boolean btnAsignarComplemento;
	private boolean validaComplemento;
	
	private Double totalFolioFactura;
	private String totalFolioFacturaStr;
	private Double totalOtrosPagos;
	private String totalOtrosPagosStr;
	private Double totalRemanenteFolioFactura;
	private String totalRemanenteFolioFacturaStr;
	
	private List<PagoComplementoFolioFacturaViewModel> listPagosFoliosFacturaView;
	private List<PagoComplementoFolioFacturaModel> listPagoComplementos;

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

	public Double getImportePagoComplemento() {
		return importePagoComplemento;
	}

	public void setImportePagoComplemento(Double importePagoComplemento) {
		this.importePagoComplemento = importePagoComplemento;
	}

	public String getImportePagoComplementoStr() {
		return importePagoComplementoStr;
	}

	public void setImportePagoComplementoStr(String importePagoComplementoStr) {
		this.importePagoComplementoStr = importePagoComplementoStr;
	}

	public Double getTotalFacturas() {
		return totalFacturas;
	}

	public void setTotalFacturas(Double totalFacturas) {
		this.totalFacturas = totalFacturas;
	}

	public String getTotalFacturasStr() {
		return totalFacturasStr;
	}

	public void setTotalFacturasStr(String totalFacturasStr) {
		this.totalFacturasStr = totalFacturasStr;
	}

	public Double getTotalComplemento() {
		return totalComplemento;
	}

	public void setTotalComplemento(Double totalComplemento) {
		this.totalComplemento = totalComplemento;
	}

	public String getTotalComplementoStr() {
		return totalComplementoStr;
	}

	public void setTotalComplementoStr(String totalComplementoStr) {
		this.totalComplementoStr = totalComplementoStr;
	}

	public String getFolioFactura() {
		return folioFactura;
	}

	public void setFolioFactura(String folioFactura) {
		this.folioFactura = folioFactura;
	}

	public boolean isExistePagoComplemento() {
		return existePagoComplemento;
	}

	public void setExistePagoComplemento(boolean existePagoComplemento) {
		this.existePagoComplemento = existePagoComplemento;
	}

	public String getMsgValidacion() {
		return msgValidacion;
	}

	public void setMsgValidacion(String msgValidacion) {
		this.msgValidacion = msgValidacion;
	}

	public List<PagoComplementoFolioFacturaViewModel> getListPagosFoliosFacturaView() {
		return listPagosFoliosFacturaView;
	}

	public void setListPagosFoliosFacturaView(List<PagoComplementoFolioFacturaViewModel> listPagosFoliosFacturaView) {
		this.listPagosFoliosFacturaView = listPagosFoliosFacturaView;
	}

	public boolean isBtnVincularPago() {
		return btnVincularPago;
	}

	public void setBtnVincularPago(boolean btnVincularPago) {
		this.btnVincularPago = btnVincularPago;
	}

	public boolean isBtnAsignarComplemento() {
		return btnAsignarComplemento;
	}

	public void setBtnAsignarComplemento(boolean btnAsignarComplemento) {
		this.btnAsignarComplemento = btnAsignarComplemento;
	}

	public boolean isValidaComplemento() {
		return validaComplemento;
	}

	public void setValidaComplemento(boolean validaComplemento) {
		this.validaComplemento = validaComplemento;
	}
	
	

	public Double getTotalFolioFactura() {
		return totalFolioFactura;
	}

	public void setTotalFolioFactura(Double totalFolioFactura) {
		this.totalFolioFactura = totalFolioFactura;
	}

	public String getTotalFolioFacturaStr() {
		return totalFolioFacturaStr;
	}

	public void setTotalFolioFacturaStr(String totalFolioFacturaStr) {
		this.totalFolioFacturaStr = totalFolioFacturaStr;
	}

	public Double getTotalOtrosPagos() {
		return totalOtrosPagos;
	}

	public void setTotalOtrosPagos(Double totalOtrosPagos) {
		this.totalOtrosPagos = totalOtrosPagos;
	}

	public String getTotalOtrosPagosStr() {
		return totalOtrosPagosStr;
	}

	public void setTotalOtrosPagosStr(String totalOtrosPagosStr) {
		this.totalOtrosPagosStr = totalOtrosPagosStr;
	}

	public Double getTotalRemanenteFolioFactura() {
		return totalRemanenteFolioFactura;
	}

	public void setTotalRemanenteFolioFactura(Double totalRemanenteFolioFactura) {
		this.totalRemanenteFolioFactura = totalRemanenteFolioFactura;
	}

	public String getTotalRemanenteFolioFacturaStr() {
		return totalRemanenteFolioFacturaStr;
	}

	public void setTotalRemanenteFolioFacturaStr(String totalRemanenteFolioFacturaStr) {
		this.totalRemanenteFolioFacturaStr = totalRemanenteFolioFacturaStr;
	}

	public List<PagoComplementoFolioFacturaModel> getListPagoComplementos() {
		return listPagoComplementos;
	}

	public void setListPagoComplementos(List<PagoComplementoFolioFacturaModel> listPagoComplementos) {
		this.listPagoComplementos = listPagoComplementos;
	}

}

package com.sodimac.facturacion.models;

public class RespuestaPac {

	private String facturaId;
	private String folio;
	private String uuid;
	private String xml;
	private String errorDesc;
	private String estatusId;

	public String getFacturaId() {
		return facturaId;
	}

	public void setFacturaId(String facturaId) {
		this.facturaId = facturaId;
	}

	public String getFolio() {
		return folio;
	}

	public void setFolio(String folio) {
		this.folio = folio;
	}

	public String getUuid() {
		return uuid;
	}

	public void setUuid(String uuid) {
		this.uuid = uuid;
	}

	public String getXml() {
		return xml;
	}

	public void setXml(String xml) {
		this.xml = xml;
	}

	public String getErrorDesc() {
		return errorDesc;
	}

	public void setErrorDesc(String errorDesc) {
		this.errorDesc = errorDesc;
	}

	public String getEstatusId() {
		return estatusId;
	}

	public void setEstatusId(String estatusId) {
		this.estatusId = estatusId;
	}

}

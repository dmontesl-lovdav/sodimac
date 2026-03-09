package com.sodimac.cfdi.dto.vista;

public class DtoFactura {

	private Integer idFolioFactura;
	private Integer idFactura;
	private String folioFactura;
	private String rfc;
	private Integer orden;

	public Integer getIdFolioFactura() {
		return idFolioFactura;
	}

	public void setIdFolioFactura(Integer idFolioFactura) {
		this.idFolioFactura = idFolioFactura;
	}

	public Integer getIdFactura() {
		return idFactura;
	}

	public void setIdFactura(Integer idFactura) {
		this.idFactura = idFactura;
	}

	public String getFolioFactura() {
		return folioFactura;
	}

	public void setFolioFactura(String folioFactura) {
		this.folioFactura = folioFactura;
	}

	public String getRfc() {
		return rfc;
	}

	public void setRfc(String rfc) {
		this.rfc = rfc;
	}

	public Integer getOrden() {
		return orden;
	}

	public void setOrden(Integer orden) {
		this.orden = orden;
	}

}

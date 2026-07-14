package com.sodimac.fiscal.api.model.dto;

public class XmlFIscalDto {

	private String uuid;
	private String rfc;
	private String versionCFDI;

	public XmlFIscalDto () {}

	public XmlFIscalDto (String uuid, String rfc, String versionCFDI ) {
		this.uuid = uuid;
		this.rfc = rfc;
		this.versionCFDI = versionCFDI;
	}

	public String getUUID() {
		return uuid;
	}
	public void setUUID(String uuid) {
		this.uuid = uuid;
	}
	public String getRfc() {
		return rfc;
	}
	public void setRfc(String rfc) {
		this.rfc = rfc;
	}
	public String getVersionCFDI() {
		return versionCFDI;
	}
	public void setVersionCFDI(String versionCFDI) {
		this.versionCFDI = versionCFDI;
	}

}

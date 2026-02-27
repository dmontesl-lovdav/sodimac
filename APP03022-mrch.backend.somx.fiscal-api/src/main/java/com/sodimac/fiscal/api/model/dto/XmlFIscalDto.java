package com.sodimac.fiscal.api.model.dto;

public class XmlFIscalDto {

	private String UUID;
	private String Rfc;
	private String VersionCFDI;
	
	public XmlFIscalDto () {}
	
	public XmlFIscalDto (String UUID, String Rfc, String VersionCFDI ) {
		this.UUID = UUID;
		this.Rfc = Rfc;
		this.VersionCFDI = VersionCFDI;
	}
	
	public String getUUID() {
		return UUID;
	}
	public void setUUID(String uUID) {
		UUID = uUID;
	}
	public String getRfc() {
		return Rfc;
	}
	public void setRfc(String rfc) {
		Rfc = rfc;
	}
	public String getVersionCFDI() {
		return VersionCFDI;
	}
	public void setVersionCFDI(String versionCFDI) {
		VersionCFDI = versionCFDI;
	}
	
	
}

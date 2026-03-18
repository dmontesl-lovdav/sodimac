package com.sodimac.wsconfiguracion.dto;


public class VersionTimbradoDto {

	private String version;

	public VersionTimbradoDto () {}
	
	public VersionTimbradoDto (String version) {
		this.version = version;
	}
	
	public String getVersion() {
		return version;
	}
	public void setVersion(String version) {
		this.version = version;
	}
	

}

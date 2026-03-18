package com.sodimac.facturacion.util.enums;

public enum EVersionCFDI {

	VERSION_33 	("3.3", 1),
	VERSION_40	("4.0", 2);
	
	private String version;
	private int id;
	
	EVersionCFDI(String version, int id) {
		this.version = version;
		this.id = id;
	}

	public String getVersion() {
		return version;
	}

	public void setVersion(String version) {
		this.version = version;
	}
	
	public int getId() {
		return id;
	}

	public void setId(int id) {
		this.id = id;
	}

	public static EVersionCFDI getVersionByDesc(String version) {
		for (EVersionCFDI ver : EVersionCFDI.values()) {
			if (ver.getVersion().equals(version)) {
				return ver;
			}
		}
		return null;
	}
}

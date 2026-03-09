package com.sodimac.cfdi.model;

public class BodyComplementoCorreoTYPE {

	private String uuid;
	private String email;
	private String emailCC;
	private String xml;
	private String razonSocial;
	private int idFacturaPac;

	public String getUuid() {
		return uuid;
	}

	public void setUuid(String uuid) {
		this.uuid = uuid;
	}

	public String getEmail() {
		return email;
	}

	public void setEmail(String email) {
		this.email = email;
	}

	public String getEmailCC() {
		return emailCC;
	}

	public void setEmailCC(String emailCC) {
		this.emailCC = emailCC;
	}

	public String getXml() {
		return xml;
	}

	public void setXml(String xml) {
		this.xml = xml;
	}

	public String getRazonSocial() {
		return razonSocial;
	}

	public void setRazonSocial(String razonSocial) {
		this.razonSocial = razonSocial;
	}

	public int getIdFacturaPac() {
		return idFacturaPac;
	}

	public void setIdFacturaPac(int idFacturaPac) {
		this.idFacturaPac = idFacturaPac;
	}

}

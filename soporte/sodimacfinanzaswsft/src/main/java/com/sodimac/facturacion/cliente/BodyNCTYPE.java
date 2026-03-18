package com.sodimac.facturacion.cliente;

public class BodyNCTYPE {
	
    protected String Documento;
    protected String Monto;
    protected String Correo;
	protected String CorreoCC;

	public String getDocumento() {
		return Documento;
	}

	public void setDocumento(String documento) {
		Documento = documento;
	}

	public String getMonto() {
		return Monto;
	}

	public void setMonto(String monto) {
		Monto = monto;
	}

	public String getCorreo() {
		return Correo;
	}

	public void setCorreo(String correo) {
		Correo = correo;
	}

	public String getCorreoCC() {
		return CorreoCC;
	}

	public void setCorreoCC(String correoCC) {
		CorreoCC = correoCC;
	}

}
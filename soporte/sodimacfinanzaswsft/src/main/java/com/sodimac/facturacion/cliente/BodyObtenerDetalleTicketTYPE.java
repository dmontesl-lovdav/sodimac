package com.sodimac.facturacion.cliente;

public class BodyObtenerDetalleTicketTYPE {
	
    protected String Documento;
    protected String Pais;
    protected String Comercio;
    protected String Canal;
    protected String Version;
    protected int IdAplicacion;
    
	public String getDocumento() {
		return Documento;
	}
	public void setDocumento(String documento) {
		Documento = documento;
	}
	public String getPais() {
		return Pais;
	}
	public void setPais(String pais) {
		Pais = pais;
	}
	public String getComercio() {
		return Comercio;
	}
	public void setComercio(String comercio) {
		Comercio = comercio;
	}
	public String getCanal() {
		return Canal;
	}
	public void setCanal(String canal) {
		Canal = canal;
	}
	public String getVersion() {
		return Version;
	}
	public void setVersion(String version) {
		Version = version;
	}
	public int getIdAplicacion() {
		return IdAplicacion;
	}
	public void setIdAplicacion(int idAplicacion) {
		IdAplicacion = idAplicacion;
	}

}
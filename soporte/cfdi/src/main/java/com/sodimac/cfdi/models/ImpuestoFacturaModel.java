package com.sodimac.cfdi.models;

public class ImpuestoFacturaModel {
	private String base;
	private String tipoImpuesto;
	private String tasa;
	private String importe;

	public String getBase() {
		return base;
	}

	public void setBase(String base) {
		this.base = base;
	}

	public String getTipoImpuesto() {
		return tipoImpuesto;
	}

	public void setTipoImpuesto(String tipoImpuesto) {
		this.tipoImpuesto = tipoImpuesto;
	}

	public String getTasa() {
		return tasa;
	}

	public void setTasa(String tasa) {
		this.tasa = tasa;
	}

	public String getImporte() {
		return importe;
	}

	public void setImporte(String importe) {
		this.importe = importe;
	}

}

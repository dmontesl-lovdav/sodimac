package com.sodimac.facturacion.models;

import java.math.BigDecimal;

public class TrasladoConcepto {

	public BigDecimal base;
	public String impuesto;
	public String tipoFactor;
	public BigDecimal tasaOCuota;
	public BigDecimal importe;
	public Long ordenador;

	public BigDecimal getBase() {
		return base;
	}

	public void setBase(BigDecimal base) {
		this.base = base;
	}

	public String getImpuesto() {
		return impuesto;
	}

	public void setImpuesto(String impuesto) {
		this.impuesto = impuesto;
	}

	public String getTipoFactor() {
		return tipoFactor;
	}

	public void setTipoFactor(String tipoFactor) {
		this.tipoFactor = tipoFactor;
	}

	public BigDecimal getTasaOCuota() {
		return tasaOCuota;
	}

	public void setTasaOCuota(BigDecimal tasaOCuota) {
		this.tasaOCuota = tasaOCuota;
	}

	public BigDecimal getImporte() {
		return importe;
	}

	public void setImporte(BigDecimal importe) {
		this.importe = importe;
	}

	public Long getOrdenador() {
		return ordenador;
	}

	public void setOrdenador(Long ordenador) {
		this.ordenador = ordenador;
	}

}

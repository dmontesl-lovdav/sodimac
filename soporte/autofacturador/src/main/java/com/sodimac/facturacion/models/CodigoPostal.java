package com.sodimac.facturacion.models;

public class CodigoPostal {

	
	private int codigopostal;
	private String c_estado;
	private String c_municipio;
	private String c_localidad;
	private int estimulofranjafronteriza;
	private String fechainiciovigencia;
	private Object fehafinvigencia;

	public int getCodigopostal() {
		return codigopostal;
	}

	public void setCodigopostal(int codigopostal) {
		this.codigopostal = codigopostal;
	}

	public String getC_estado() {
		return c_estado;
	}

	public void setC_estado(String c_estado) {
		this.c_estado = c_estado;
	}

	public String getC_municipio() {
		return c_municipio;
	}

	public void setC_municipio(String c_municipio) {
		this.c_municipio = c_municipio;
	}

	public String getC_localidad() {
		return c_localidad;
	}

	public void setC_localidad(String c_localidad) {
		this.c_localidad = c_localidad;
	}

	public int getEstimulofranjafronteriza() {
		return estimulofranjafronteriza;
	}

	public void setEstimulofranjafronteriza(int estimulofranjafronteriza) {
		this.estimulofranjafronteriza = estimulofranjafronteriza;
	}

	public String getFechainiciovigencia() {
		return fechainiciovigencia;
	}

	public void setFechainiciovigencia(String fechainiciovigencia) {
		this.fechainiciovigencia = fechainiciovigencia;
	}

	public Object getFehafinvigencia() {
		return fehafinvigencia;
	}

	public void setFehafinvigencia(Object fehafinvigencia) {
		this.fehafinvigencia = fehafinvigencia;
	}

}

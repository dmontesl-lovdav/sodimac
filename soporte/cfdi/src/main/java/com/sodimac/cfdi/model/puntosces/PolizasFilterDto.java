package com.sodimac.cfdi.model.puntosces;

public class PolizasFilterDto {
	
	private Integer estatus;
	private Integer idModulo;
	private Integer empresa;
	private String cabecera;
	private String cuentaContable;
	private String descripcion;
	private String moneda;
	private Double tipoCambio;
	private String sistemaOrigen;
	private String tipoUso;
	private String indicadorImpuesto;
	private Integer tipoTransaccion;
	private String claseDoc;
	private String sucursal;
	
	public Integer getEstatus() {
		return estatus;
	}

	public void setEstatus(Integer estatus) {
		this.estatus = estatus;
	}

	public Integer getIdModulo() {
		return idModulo;
	}

	public void setIdModulo(Integer idModulo) {
		this.idModulo = idModulo;
	}

	public Integer getEmpresa() {
		return empresa;
	}

	public void setEmpresa(Integer empresa) {
		this.empresa = empresa;
	}

	public String getCabecera() {
		return cabecera;
	}

	public void setCabecera(String cabecera) {
		this.cabecera = cabecera;
	}

	public String getCuentaContable() {
		return cuentaContable;
	}

	public void setCuentaContable(String cuentaContable) {
		this.cuentaContable = cuentaContable;
	}

	public String getDescripcion() {
		return descripcion;
	}

	public void setDescripcion(String descripcion) {
		this.descripcion = descripcion;
	}

	public String getMoneda() {
		return moneda;
	}

	public void setMoneda(String moneda) {
		this.moneda = moneda;
	}

	public Double getTipoCambio() {
		return tipoCambio;
	}

	public void setTipoCambio(Double tipoCambio) {
		this.tipoCambio = tipoCambio;
	}

	public String getSistemaOrigen() {
		return sistemaOrigen;
	}

	public void setSistemaOrigen(String sistemaOrigen) {
		this.sistemaOrigen = sistemaOrigen;
	}

	public String getTipoUso() {
		return tipoUso;
	}

	public void setTipoUso(String tipoUso) {
		this.tipoUso = tipoUso;
	}

	public String getIndicadorImpuesto() {
		return indicadorImpuesto;
	}

	public void setIndicadorImpuesto(String indicadorImpuesto) {
		this.indicadorImpuesto = indicadorImpuesto;
	}

	public Integer getTipoTransaccion() {
		return tipoTransaccion;
	}

	public void setTipoTransaccion(Integer tipoTransaccion) {
		this.tipoTransaccion = tipoTransaccion;
	}

	public String getClaseDoc() {
		return claseDoc;
	}

	public void setClaseDoc(String claseDoc) {
		this.claseDoc = claseDoc;
	}

	public String getSucursal() {
		return sucursal;
	}

	public void setSucursal(String sucursal) {
		this.sucursal = sucursal;
	}

}

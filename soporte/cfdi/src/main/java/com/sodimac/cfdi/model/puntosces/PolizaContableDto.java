package com.sodimac.cfdi.model.puntosces;

import java.util.Date;

public class PolizaContableDto {

	private Integer idConfigContable;
	private String idModulo;
	private String empresa;
	private String cabecera;
	private String posicion;
	private String cuentaContable;
	private String debitoCredito;
	private String descripcion;
	private String moneda;
	private Double tipoCambio;
	private String sistemaOrigen;
	private String origenEtl;
	private String tipoUso;
	private String indicadorImpuesto;
	private String tipoTransaccion;
	private String claseDoc;
	private String sucursal;
	private Double impuesto;
	private String tipoTransaccionContable;
	private String tipoImpuesto;
	private String tasaImpuesto;
	private Integer estatus;
	private Integer usuario;
	private Date fechaRegistro;
	private Date fechaActualizacion;

	public PolizaContableDto() {

	}

	public Integer getIdConfigContable() {
		return idConfigContable;
	}

	public void setIdConfigContable(Integer idConfigContable) {
		this.idConfigContable = idConfigContable;
	}

	public String getIdModulo() {
		return idModulo;
	}

	public void setIdModulo(String idModulo) {
		this.idModulo = idModulo;
	}

	public String getEmpresa() {
		return empresa;
	}

	public void setEmpresa(String empresa) {
		this.empresa = empresa;
	}

	public String getCabecera() {
		return cabecera;
	}

	public void setCabecera(String cabecera) {
		this.cabecera = cabecera;
	}

	public String getPosicion() {
		return posicion;
	}

	public void setPosicion(String posicion) {
		this.posicion = posicion;
	}

	public String getCuentaContable() {
		return cuentaContable;
	}

	public void setCuentaContable(String cuentaContable) {
		this.cuentaContable = cuentaContable;
	}

	public String getDebitoCredito() {
		return debitoCredito;
	}

	public void setDebitoCredito(String debitoCredito) {
		this.debitoCredito = debitoCredito;
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

	public String getOrigenEtl() {
		return origenEtl;
	}

	public void setOrigenEtl(String origenEtl) {
		this.origenEtl = origenEtl;
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

	public String getTipoTransaccion() {
		return tipoTransaccion;
	}

	public void setTipoTransaccion(String tipoTransaccion) {
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

	public Double getImpuesto() {
		return impuesto;
	}

	public void setImpuesto(Double impuesto) {
		this.impuesto = impuesto;
	}

	public String getTipoTransaccionContable() {
		return tipoTransaccionContable;
	}

	public void setTipoTransaccionContable(String tipoTransaccionContable) {
		this.tipoTransaccionContable = tipoTransaccionContable;
	}

	public String getTipoImpuesto() {
		return tipoImpuesto;
	}

	public void setTipoImpuesto(String tipoImpuesto) {
		this.tipoImpuesto = tipoImpuesto;
	}

	public String getTasaImpuesto() {
		return tasaImpuesto;
	}

	public void setTasaImpuesto(String tasaImpuesto) {
		this.tasaImpuesto = tasaImpuesto;
	}

	public Integer getEstatus() {
		return estatus;
	}

	public void setEstatus(Integer estatus) {
		this.estatus = estatus;
	}

	public Integer getUsuario() {
		return usuario;
	}

	public void setUsuario(Integer usuario) {
		this.usuario = usuario;
	}

	public Date getFechaRegistro() {
		return fechaRegistro;
	}

	public void setFechaRegistro(Date fechaRegistro) {
		this.fechaRegistro = fechaRegistro;
	}

	public Date getFechaActualizacion() {
		return fechaActualizacion;
	}

	public void setFechaActualizacion(Date fechaActualizacion) {
		this.fechaActualizacion = fechaActualizacion;
	}

	@Override
	public String toString() {
		return "PolizaContableDto [idConfigContable=" + idConfigContable + ", idModulo=" + idModulo + ", empresa="
				+ empresa + ", cabecera=" + cabecera + ", posicion=" + posicion + ", cuentaContable=" + cuentaContable
				+ ", debitoCredito=" + debitoCredito + ", descripcion=" + descripcion + ", moneda=" + moneda
				+ ", tipoCambio=" + tipoCambio + ", sistemaOrigen=" + sistemaOrigen + ", origenEtl=" + origenEtl
				+ ", tipoUso=" + tipoUso + ", indicadorImpuesto=" + indicadorImpuesto + ", tipoTransaccion="
				+ tipoTransaccion + ", claseDoc=" + claseDoc + ", sucursal=" + sucursal + ", impuesto=" + impuesto
				+ ", tipoTransaccionContable=" + tipoTransaccionContable + ", tipoImpuesto=" + tipoImpuesto
				+ ", tasaImpuesto=" + tasaImpuesto + ", estatus=" + estatus + ", usuario=" + usuario
				+ ", fechaRegistro=" + fechaRegistro + ", fechaActualizacion=" + fechaActualizacion + "]";
	}

}

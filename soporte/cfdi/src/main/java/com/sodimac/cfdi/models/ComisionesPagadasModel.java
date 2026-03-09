package com.sodimac.cfdi.models;

import java.util.Date;

public class ComisionesPagadasModel {
	
	private Integer idComisionesPagadas;
	private Date fechaTrx;
	private String fechaTrxStr;
	private Integer tipoTrx;
	private Integer numTienda;
	private String numTrx;
	private Integer numCaja;
	private Double montoPago;
	private String codComercio;
	private String numTarjeta;
	private Integer numCuotas;
	private String codAutorizacion;
	private String tipoTarjeta;
	private String pasarela;
	private Integer codBancoEmisor;
	private Integer codMarcaTarjeta;
	private Double comisionUsoPorc;
	private Double comisionUsoMonto;
	private Double porcDescto;
	private Double porcSodimacDescto;
	private Double porcEmisorDescto;
	private Double sobretasa;
	private String indPromocion;
	private Integer factorCambioMoneda;
	
	public Integer getIdComisionesPagadas() {
		return idComisionesPagadas;
	}

	public void setIdComisionesPagadas(Integer idComisionesPagadas) {
		this.idComisionesPagadas = idComisionesPagadas;
	}

	public Date getFechaTrx() {
		return fechaTrx;
	}

	public void setFechaTrx(Date fechaTrx) {
		this.fechaTrx = fechaTrx;
	}

	public String getFechaTrxStr() {
		return fechaTrxStr;
	}

	public void setFechaTrxStr(String fechaTrxStr) {
		this.fechaTrxStr = fechaTrxStr;
	}

	public Integer getTipoTrx() {
		return tipoTrx;
	}

	public void setTipoTrx(Integer tipoTrx) {
		this.tipoTrx = tipoTrx;
	}

	public Integer getNumTienda() {
		return numTienda;
	}

	public void setNumTienda(Integer numTienda) {
		this.numTienda = numTienda;
	}

	public String getNumTrx() {
		return numTrx;
	}

	public void setNumTrx(String numTrx) {
		this.numTrx = numTrx;
	}

	public Integer getNumCaja() {
		return numCaja;
	}

	public void setNumCaja(Integer numCaja) {
		this.numCaja = numCaja;
	}

	public Double getMontoPago() {
		return montoPago;
	}

	public void setMontoPago(Double montoPago) {
		this.montoPago = montoPago;
	}

	public String getCodComercio() {
		return codComercio;
	}

	public void setCodComercio(String codComercio) {
		this.codComercio = codComercio;
	}

	public String getNumTarjeta() {
		return numTarjeta;
	}

	public void setNumTarjeta(String numTarjeta) {
		this.numTarjeta = numTarjeta;
	}

	public Integer getNumCuotas() {
		return numCuotas;
	}

	public void setNumCuotas(Integer numCuotas) {
		this.numCuotas = numCuotas;
	}

	public String getCodAutorizacion() {
		return codAutorizacion;
	}

	public void setCodAutorizacion(String codAutorizacion) {
		this.codAutorizacion = codAutorizacion;
	}

	public String getTipoTarjeta() {
		return tipoTarjeta;
	}

	public void setTipoTarjeta(String tipoTarjeta) {
		this.tipoTarjeta = tipoTarjeta;
	}

	public String getPasarela() {
		return pasarela;
	}

	public void setPasarela(String pasarela) {
		this.pasarela = pasarela;
	}

	public Integer getCodBancoEmisor() {
		return codBancoEmisor;
	}

	public void setCodBancoEmisor(Integer codBancoEmisor) {
		this.codBancoEmisor = codBancoEmisor;
	}

	public Integer getCodMarcaTarjeta() {
		return codMarcaTarjeta;
	}

	public void setCodMarcaTarjeta(Integer codMarcaTarjeta) {
		this.codMarcaTarjeta = codMarcaTarjeta;
	}

	public Double getComisionUsoPorc() {
		return comisionUsoPorc;
	}

	public void setComisionUsoPorc(Double comisionUsoPorc) {
		this.comisionUsoPorc = comisionUsoPorc;
	}

	public Double getComisionUsoMonto() {
		return comisionUsoMonto;
	}

	public void setComisionUsoMonto(Double comisionUsoMonto) {
		this.comisionUsoMonto = comisionUsoMonto;
	}

	public Double getPorcDescto() {
		return porcDescto;
	}

	public void setPorcDescto(Double porcDescto) {
		this.porcDescto = porcDescto;
	}

	public Double getPorcSodimacDescto() {
		return porcSodimacDescto;
	}

	public void setPorcSodimacDescto(Double porcSodimacDescto) {
		this.porcSodimacDescto = porcSodimacDescto;
	}

	public Double getPorcEmisorDescto() {
		return porcEmisorDescto;
	}

	public void setPorcEmisorDescto(Double porcEmisorDescto) {
		this.porcEmisorDescto = porcEmisorDescto;
	}

	public Double getSobretasa() {
		return sobretasa;
	}

	public void setSobretasa(Double sobretasa) {
		this.sobretasa = sobretasa;
	}

	public String getIndPromocion() {
		return indPromocion;
	}

	public void setIndPromocion(String indPromocion) {
		this.indPromocion = indPromocion;
	}

	public Integer getFactorCambioMoneda() {
		return factorCambioMoneda;
	}

	public void setFactorCambioMoneda(Integer factorCambioMoneda) {
		this.factorCambioMoneda = factorCambioMoneda;
	}

}

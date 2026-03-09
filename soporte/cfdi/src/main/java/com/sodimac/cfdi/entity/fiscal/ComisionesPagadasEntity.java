package com.sodimac.cfdi.entity.fiscal;

import java.util.Date;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;

@Entity
@Table(name = "comisionesPagadas")
public class ComisionesPagadasEntity {

	@Id
	@Column(name = "idComisionesPagadas")
	private Integer idComisionesPagadas;

	@Column(name = "fechaTrx")
	private Date fechaTrx;
	
	@Column(name = "tipoTrx")
	private Integer tipoTrx;
	
	@Column(name = "numTienda")
	private Integer numTienda;
	
	@Column(name = "numTrx")
	private String numTrx;
	
	@Column(name = "numCaja")
	private Integer numCaja;
	
	@Column(name = "montoPago")
	private Double montoPago;
	
	@Column(name = "codComercio")
	private String codComercio;
	
	@Column(name = "numTarjeta")
	private String numTarjeta;
	
	@Column(name = "numCuotas")
	private Integer numCuotas;
	
	@Column(name = "codAutorizacion")
	private String codAutorizacion;
	
	@Column(name = "tipoTarjeta")
	private String tipoTarjeta;
	
	@Column(name = "pasarela")
	private String pasarela;
	
	@Column(name = "codBancoEmisor")
	private Integer codBancoEmisor;
	
	@Column(name = "codMarcaTarjeta")
	private Integer codMarcaTarjeta;
	
	@Column(name = "comisionUsoPorc")
	private Double comisionUsoPorc;
	
	@Column(name = "comisionUsoMonto")
	private Double comisionUsoMonto;
	
	@Column(name = "porcDescto")
	private Double porcDescto;
	
	@Column(name = "porcSodimacDescto")
	private Double porcSodimacDescto;

	@Column(name = "porcEmisorDescto")
	private Double porcEmisorDescto;
	
	@Column(name = "sobretasa")
	private Double sobretasa;
	
	@Column(name = "indPromocion")
	private String indPromocion;
	
	@Column(name = "factorCambioMoneda")
	private Integer factorCambioMoneda;

	public Date getFechaTrx() {
		return fechaTrx;
	}

	public void setFechaTrx(Date fechaTrx) {
		this.fechaTrx = fechaTrx;
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

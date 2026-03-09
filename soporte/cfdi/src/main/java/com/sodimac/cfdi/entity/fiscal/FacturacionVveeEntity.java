package com.sodimac.cfdi.entity.fiscal;

import java.util.Date;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;

@Entity
@Table(name = "facturacionVvee")
public class FacturacionVveeEntity {

	@Id
	@Column(name = "idFacturacionVvee")
	private Integer idFacturacionVvee;

	@Column(name = "numTrx")
	private String numTrx;
	
	@Column(name = "numDocCanal")
	private Integer numDocCanal;
	
	@Column(name = "nroGuia")
	private String nroGuia;
	
	@Column(name = "nroGuiaProv")
	private String nroGuiaProv;
	
	@Column(name = "tipoFactura")
	private String tipoFactura;
	
	@Column(name = "nomObra")
	private String nomObra;
	
	@Column(name = "contactoObra")
	private String contactoObra;
	
	@Column(name = "email")
	private String email;
	
	@Column(name = "cfdi")
	private String cfdi;
	
	@Column(name = "rfc")
	private String rfc;
	
	@Column(name = "orden")
	private String orden;
	
	@Column(name = "rSocial")
	private String rSocial;
	
	@Column(name = "codigoPostal")
	private String codigoPostal;
	
	@Column(name = "regimenFiscal")
	private String regimenFiscal;
	
	@Column(name = "fecha")
	private Date fecha;
	
	@Column(name = "numFactura")
	private String numFactura;
	
	@Column(name = "numTicket")
	private String numTicket;
	
	@Column(name = "nroSerie")
	private String nroSerie;
	
	@Column(name = "nroFolio")
	private Integer nroFolio;
	
	@Column(name = "numTienda")
	private Integer numTienda;

	public Integer getIdFacturacionVvee() {
		return idFacturacionVvee;
	}

	public void setIdFacturacionVvee(Integer idFacturacionVvee) {
		this.idFacturacionVvee = idFacturacionVvee;
	}

	public String getNumTrx() {
		return numTrx;
	}

	public void setNumTrx(String numTrx) {
		this.numTrx = numTrx;
	}

	public Integer getNumDocCanal() {
		return numDocCanal;
	}

	public void setNumDocCanal(Integer numDocCanal) {
		this.numDocCanal = numDocCanal;
	}

	public String getNroGuia() {
		return nroGuia;
	}

	public void setNroGuia(String nroGuia) {
		this.nroGuia = nroGuia;
	}

	public String getNroGuiaProv() {
		return nroGuiaProv;
	}

	public void setNroGuiaProv(String nroGuiaProv) {
		this.nroGuiaProv = nroGuiaProv;
	}

	public String getTipoFactura() {
		return tipoFactura;
	}

	public void setTipoFactura(String tipoFactura) {
		this.tipoFactura = tipoFactura;
	}

	public String getNomObra() {
		return nomObra;
	}

	public void setNomObra(String nomObra) {
		this.nomObra = nomObra;
	}

	public String getContactoObra() {
		return contactoObra;
	}

	public void setContactoObra(String contactoObra) {
		this.contactoObra = contactoObra;
	}

	public String getEmail() {
		return email;
	}

	public void setEmail(String email) {
		this.email = email;
	}

	public String getCfdi() {
		return cfdi;
	}

	public void setCfdi(String cfdi) {
		this.cfdi = cfdi;
	}

	public String getRfc() {
		return rfc;
	}

	public void setRfc(String rfc) {
		this.rfc = rfc;
	}

	public String getOrden() {
		return orden;
	}

	public void setOrden(String orden) {
		this.orden = orden;
	}

	public String getrSocial() {
		return rSocial;
	}

	public void setrSocial(String rSocial) {
		this.rSocial = rSocial;
	}

	public String getCodigoPostal() {
		return codigoPostal;
	}

	public void setCodigoPostal(String codigoPostal) {
		this.codigoPostal = codigoPostal;
	}

	public String getRegimenFiscal() {
		return regimenFiscal;
	}

	public void setRegimenFiscal(String regimenFiscal) {
		this.regimenFiscal = regimenFiscal;
	}

	public Date getFecha() {
		return fecha;
	}

	public void setFecha(Date fecha) {
		this.fecha = fecha;
	}

	public String getNumFactura() {
		return numFactura;
	}

	public void setNumFactura(String numFactura) {
		this.numFactura = numFactura;
	}

	public String getNumTicket() {
		return numTicket;
	}

	public void setNumTicket(String numTicket) {
		this.numTicket = numTicket;
	}

	public String getNroSerie() {
		return nroSerie;
	}

	public void setNroSerie(String nroSerie) {
		this.nroSerie = nroSerie;
	}

	public Integer getNroFolio() {
		return nroFolio;
	}

	public void setNroFolio(Integer nroFolio) {
		this.nroFolio = nroFolio;
	}

	public Integer getNumTienda() {
		return numTienda;
	}

	public void setNumTienda(Integer numTienda) {
		this.numTienda = numTienda;
	}
}

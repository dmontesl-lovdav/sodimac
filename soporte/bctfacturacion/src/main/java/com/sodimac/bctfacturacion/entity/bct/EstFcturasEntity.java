package com.sodimac.bctfacturacion.entity.bct;

import java.util.Date;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;

@Entity
@Table(name = "EST_FACTURAS")
public class EstFcturasEntity {

	@Id
	@Column(name = "NUM_FACTURA")
	private String numFactura;
	
	@Column(name = "NUM_TRX")
	private String numTrx;

	@Column(name = "FECHA")
	private Date fecha;

	@Column(name = "NUM_TICKET")
	private String numTicket;

	@Column(name = "COD_ESTADO")
	private Integer codEstado;

	@Column(name = "DET_ESTADO")
	private String detEstado;

	@Column(name = "NRO_SERIE")
	private String nroSerie;

	@Column(name = "NRO_FOLIO")
	private Integer nroFolio;

	@Column(name = "NUM_TIENDA")
	private Integer numTienda;

	@Column(name = "FECHA_CIERRE")
	private Date fechaCierre;

	@Column(name = "EST_FACT")
	private Integer estFact;

	public String getNumTrx() {
		return numTrx;
	}

	public void setNumTrx(String numTrx) {
		this.numTrx = numTrx;
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

	public Integer getCodEstado() {
		return codEstado;
	}

	public void setCodEstado(Integer codEstado) {
		this.codEstado = codEstado;
	}

	public String getDetEstado() {
		return detEstado;
	}

	public void setDetEstado(String detEstado) {
		this.detEstado = detEstado;
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

	public Date getFechaCierre() {
		return fechaCierre;
	}

	public void setFechaCierre(Date fechaCierre) {
		this.fechaCierre = fechaCierre;
	}

	public Integer getEstFact() {
		return estFact;
	}

	public void setEstFact(Integer estFact) {
		this.estFact = estFact;
	}

}

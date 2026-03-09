package com.sodimac.cfdi.entity.fiscal;

import java.util.Date;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Table;

@Entity
@Table(name = "pagocomplementofoliofactura")
public class PagoComplementoFolioFacturaEntity {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	@Column(name = "idPagoComplementoFolioFactura")
	private Integer idPagoComplementoFolioFactura;

	@Column(name = "idPagoComplemento")
	private Integer idPagoComplemento;

	@Column(name = "idFolioFactura")
	private Integer idFolioFactura;

	@Column(name = "totalFolioFactura")
	private Double totalFolioFactura;

	@Column(name = "montoPago")
	private Double montoPago;

	@Column(name = "fechaRegistro")
	private Date fechaRegistro;

	@Column(name = "estatus")
	private Integer estatus;

	public Integer getIdPagoComplementoFolioFactura() {
		return idPagoComplementoFolioFactura;
	}

	public void setIdPagoComplementoFolioFactura(Integer idPagoComplementoFolioFactura) {
		this.idPagoComplementoFolioFactura = idPagoComplementoFolioFactura;
	}

	public Integer getIdPagoComplemento() {
		return idPagoComplemento;
	}

	public void setIdPagoComplemento(Integer idPagoComplemento) {
		this.idPagoComplemento = idPagoComplemento;
	}

	public Integer getIdFolioFactura() {
		return idFolioFactura;
	}

	public void setIdFolioFactura(Integer idFolioFactura) {
		this.idFolioFactura = idFolioFactura;
	}

	public Double getTotalFolioFactura() {
		return totalFolioFactura;
	}

	public void setTotalFolioFactura(Double totalFolioFactura) {
		this.totalFolioFactura = totalFolioFactura;
	}

	public Double getMontoPago() {
		return montoPago;
	}

	public void setMontoPago(Double montoPago) {
		this.montoPago = montoPago;
	}

	public Date getFechaRegistro() {
		return fechaRegistro;
	}

	public void setFechaRegistro(Date fechaRegistro) {
		this.fechaRegistro = fechaRegistro;
	}

	public Integer getEstatus() {
		return estatus;
	}

	public void setEstatus(Integer estatus) {
		this.estatus = estatus;
	}

}

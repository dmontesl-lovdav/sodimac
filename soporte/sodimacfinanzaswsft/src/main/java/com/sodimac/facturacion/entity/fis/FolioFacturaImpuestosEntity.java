package com.sodimac.facturacion.entity.fis;

import java.math.BigDecimal;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;

@Entity
@Table(name = "foliofacturaimpuestos")
public class FolioFacturaImpuestosEntity {

	@Id
	@Column(name = "idFolioFacturaImpuestos")
	private int idFolioFacturaImpuestos;

	@Column(name = "idFolioFacturaDet")
	private int idFolioFacturaDet;

	@Column(name = "idFactura")
	private int idFactura;

	@Column(name = "base", precision=30, scale=12)
	private BigDecimal base;

	@Column(name = "tipoImpuesto")
	private String tipoImpuesto;

	@Column(name = "tasa", precision=11, scale=4)
	private BigDecimal tasa;

	@Column(name = "monto", precision=30, scale=12)
	private BigDecimal monto;
	
	public FolioFacturaImpuestosEntity() {

	}

	public int getIdFolioFacturaImpuestos() {
		return idFolioFacturaImpuestos;
	}

	public void setIdFolioFacturaImpuestos(int idFolioFacturaImpuestos) {
		this.idFolioFacturaImpuestos = idFolioFacturaImpuestos;
	}

	public int getIdFolioFacturaDet() {
		return idFolioFacturaDet;
	}

	public void setIdFolioFacturaDet(int idFolioFacturaDet) {
		this.idFolioFacturaDet = idFolioFacturaDet;
	}

	public int getIdFactura() {
		return idFactura;
	}

	public void setIdFactura(int idFactura) {
		this.idFactura = idFactura;
	}

	public BigDecimal getBase() {
		return base;
	}

	public void setBase(BigDecimal base) {
		this.base = base;
	}

	public String getTipoImpuesto() {
		return tipoImpuesto;
	}

	public void setTipoImpuesto(String tipoImpuesto) {
		this.tipoImpuesto = tipoImpuesto;
	}

	public BigDecimal getTasa() {
		return tasa;
	}

	public void setTasa(BigDecimal tasa) {
		this.tasa = tasa;
	}

	public BigDecimal getMonto() {
		return monto;
	}

	public void setMonto(BigDecimal monto) {
		this.monto = monto;
	}

}

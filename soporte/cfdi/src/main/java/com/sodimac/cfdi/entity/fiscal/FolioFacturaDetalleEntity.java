package com.sodimac.cfdi.entity.fiscal;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Table;

@Entity
@Table(name = "foliofacturadet")
public class FolioFacturaDetalleEntity {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	@Column(name = "idFolioFacturaDet")
	private Integer idFolioFacturaDet;

	@Column(name = "idFolioFactura")
	private Integer idFolioFactura;

	@Column(name = "idFactura")
	private Integer idFactura;

	@Column(name = "uuid")
	private String uuid;

	@Column(name = "montoFactura")
	private Double montoFactura;

	@Column(name = "uuidRelacionado")
	private String uuidRelacionado;
	
	@Column(name = "orden")
	private Integer orden;

	public Integer getIdFolioFacturaDet() {
		return idFolioFacturaDet;
	}

	public void setIdFolioFacturaDet(Integer idFolioFacturaDet) {
		this.idFolioFacturaDet = idFolioFacturaDet;
	}

	public Integer getIdFolioFactura() {
		return idFolioFactura;
	}

	public void setIdFolioFactura(Integer idFolioFactura) {
		this.idFolioFactura = idFolioFactura;
	}

	public Integer getIdFactura() {
		return idFactura;
	}

	public void setIdFactura(Integer idFactura) {
		this.idFactura = idFactura;
	}

	public String getUuid() {
		return uuid;
	}

	public void setUuid(String uuid) {
		this.uuid = uuid;
	}

	public Double getMontoFactura() {
		return montoFactura;
	}

	public void setMontoFactura(Double montoFactura) {
		this.montoFactura = montoFactura;
	}

	public String getUuidRelacionado() {
		return uuidRelacionado;
	}

	public void setUuidRelacionado(String uuidRelacionado) {
		this.uuidRelacionado = uuidRelacionado;
	}

	public Integer getOrden() {
		return orden;
	}

	public void setOrden(Integer orden) {
		this.orden = orden;
	}
}

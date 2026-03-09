package com.sodimac.cfdi.entity.fiscal;

import java.util.Date;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Table;

@Entity
@Table(name = "foliofactura")
public class FolioFacturaEntity {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	@Column(name = "idFolioFactura")
	private Integer idFolioFactura;

	@Column(name = "folioFactura")
	private Integer folioFactura;

	@Column(name = "rfc")
	private String rfc;

	@Column(name = "razonSocial")
	private String razonSocial;

	@Column(name = "fechaRegistro")
	private Date fechaRegistro;

	@Column(name = "estatus")
	private Integer estatus;

	public Integer getIdFolioFactura() {
		return idFolioFactura;
	}

	public void setIdFolioFactura(Integer idFolioFactura) {
		this.idFolioFactura = idFolioFactura;
	}

	public Integer getFolioFactura() {
		return folioFactura;
	}

	public void setFolioFactura(Integer folioFactura) {
		this.folioFactura = folioFactura;
	}

	public String getRfc() {
		return rfc;
	}

	public void setRfc(String rfc) {
		this.rfc = rfc;
	}

	public String getRazonSocial() {
		return razonSocial;
	}

	public void setRazonSocial(String razonSocial) {
		this.razonSocial = razonSocial;
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

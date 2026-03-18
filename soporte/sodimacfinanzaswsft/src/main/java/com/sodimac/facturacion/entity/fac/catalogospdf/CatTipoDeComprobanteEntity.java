package com.sodimac.facturacion.entity.fac.catalogospdf;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;

@Entity
@Table(name = "catTipoDeComprobante")
public class CatTipoDeComprobanteEntity {

	@Id
	@Column(name = "id")
	private int id;

	@Column(name = "idComprobante", length=1, columnDefinition="CHAR")
	private String idComprobante;

	@Column(name = "descripcionSat")
	private String descripcionSat;

	@Column(name = "descripcionTimbrado")
	private String descripcionTimbrado;

	public CatTipoDeComprobanteEntity() {

	}

	public int getId() {
		return id;
	}

	public void setId(int id) {
		this.id = id;
	}

	public String getIdComprobante() {
		return idComprobante;
	}

	public void setIdComprobante(String idComprobante) {
		this.idComprobante = idComprobante;
	}

	public String getDescripcionSat() {
		return descripcionSat;
	}

	public void setDescripcionSat(String descripcionSat) {
		this.descripcionSat = descripcionSat;
	}

	public String getDescripcionTimbrado() {
		return descripcionTimbrado;
	}

	public void setDescripcionTimbrado(String descripcionTimbrado) {
		this.descripcionTimbrado = descripcionTimbrado;
	}

	@Override
	public String toString() {
		return "CatTipoDeComprobanteEntity [id=" + id + ", idComprobante=" + idComprobante + ", descripcionSat="
				+ descripcionSat + ", descripcionTimbrado=" + descripcionTimbrado + "]";
	}
	
}

package com.sodimac.rebates.model.entity;

import java.io.Serializable;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;

/**
 *
 * @author david.montes
 */
@Entity
@Table(name = "RebateProveedor")
public class RebateProveedorEntity implements Serializable {

	private static final long serialVersionUID = 1L;
	@Id
	@Column(name = "IdRebateProveedor")
	private Integer idRebateProveedor;

	@Column(name = "CodigoProveedor")
	private String codigoProveedor;

	@Column(name = "NombreProveedor")
	private String nombreProveedor;

	@Column(name = "Origen")
	private String origen;

	@Column(name = "RUTDV")
	private String rutv;

	@Column(name = "RFC")
	private String rfc;

	@Column(name = "Correo")
	private String correo;

	@Column(name = "RegimenFiscal")
	private String regimenFiscal;

	@Column(name = "CodigoPostal")
	private String codigoPostal;

	public Integer getIdRebateProveedor() {
		return idRebateProveedor;
	}

	public void setIdRebateProveedor(Integer idRebateProveedor) {
		this.idRebateProveedor = idRebateProveedor;
	}

	public String getCodigoProveedor() {
		return codigoProveedor;
	}

	public void setCodigoProveedor(String codigoProveedor) {
		this.codigoProveedor = codigoProveedor;
	}

	public String getNombreProveedor() {
		return nombreProveedor;
	}

	public void setNombreProveedor(String nombreProveedor) {
		this.nombreProveedor = nombreProveedor;
	}

	public String getOrigen() {
		return origen;
	}

	public void setOrigen(String origen) {
		this.origen = origen;
	}

	public String getRutv() {
		return rutv;
	}

	public void setRutv(String rutv) {
		this.rutv = rutv;
	}

	public String getRfc() {
		return rfc;
	}

	public void setRfc(String rfc) {
		this.rfc = rfc;
	}

	public String getCorreo() {
		return correo;
	}

	public void setCorreo(String correo) {
		this.correo = correo;
	}

	public String getRegimenFiscal() {
		return regimenFiscal;
	}

	public void setRegimenFiscal(String regimenFiscal) {
		this.regimenFiscal = regimenFiscal;
	}

	public String getCodigoPostal() {
		return codigoPostal;
	}

	public void setCodigoPostal(String codigoPostal) {
		this.codigoPostal = codigoPostal;
	}

}

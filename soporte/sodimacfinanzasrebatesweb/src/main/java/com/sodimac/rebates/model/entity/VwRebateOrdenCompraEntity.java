package com.sodimac.rebates.model.entity;

import java.io.Serializable;
import java.util.Date;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;

/**
 *
 * @author david.montes
 */
@Entity
@Table(name = "vw_rebate_orden_compra")
public class VwRebateOrdenCompraEntity implements Serializable {

	private static final long serialVersionUID = 1L;
	@Id
	@Column(name = "NUM_OC")
	private Integer ordenCompra;

	@Column(name = "NUM_PROVEEDOR")
	private String proveedor;

	@Column(name = "NOM_PROVEEDOR")
	private String nombreProveedor;
	
	@Column(name = "FAMILIA")
	private String familia;

	@Column(name = "SKU")
	private String sku;
	
	@Column(name = "SKU_DESCRIPCION")
	private String descripcionSku;

	@Column(name = "FechaRecepcion")
	private Date fechaRecepcion;

	public Integer getOrdenCompra() {
		return ordenCompra;
	}

	public void setOrdenCompra(Integer ordenCompra) {
		this.ordenCompra = ordenCompra;
	}

	public String getProveedor() {
		return proveedor;
	}

	public void setProveedor(String proveedor) {
		this.proveedor = proveedor;
	}

	public String getNombreProveedor() {
		return nombreProveedor;
	}

	public void setNombreProveedor(String nombreProveedor) {
		this.nombreProveedor = nombreProveedor;
	}

	public String getFamilia() {
		return familia;
	}

	public void setFamilia(String familia) {
		this.familia = familia;
	}

	public String getSku() {
		return sku;
	}

	public void setSku(String sku) {
		this.sku = sku;
	}

	public String getDescripcionSku() {
		return descripcionSku;
	}

	public void setDescripcionSku(String descripcionSku) {
		this.descripcionSku = descripcionSku;
	}

	public Date getFechaRecepcion() {
		return fechaRecepcion;
	}

	public void setFechaRecepcion(Date fechaRecepcion) {
		this.fechaRecepcion = fechaRecepcion;
	}

}

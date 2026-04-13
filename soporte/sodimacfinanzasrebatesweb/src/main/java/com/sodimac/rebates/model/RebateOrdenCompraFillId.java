package com.sodimac.rebates.model;

import java.io.Serializable;

import javax.persistence.Column;
import javax.persistence.Embeddable;

@Embeddable
public class RebateOrdenCompraFillId implements Serializable {
	
	private static final long serialVersionUID = 4923142990860131561L;

	@Column(name = "sku")
	private String sku;
	
	@Column(name = "NumeroTienda")
	private Integer numeroTienda;
	
	@Column(name = "NumeroProveedor")
	private String numeroProveedor;
	
	@Column(name = "NumeroOrdenCompra")
	private Integer numeroOrdenCompra;

	public String getSku() {
		return sku;
	}

	public void setSku(String sku) {
		this.sku = sku;
	}

	public Integer getNumeroTienda() {
		return numeroTienda;
	}

	public void setNumeroTienda(Integer numeroTienda) {
		this.numeroTienda = numeroTienda;
	}

	public String getNumeroProveedor() {
		return numeroProveedor;
	}

	public void setNumeroProveedor(String numeroProveedor) {
		this.numeroProveedor = numeroProveedor;
	}

	public Integer getNumeroOrdenCompra() {
		return numeroOrdenCompra;
	}

	public void setNumeroOrdenCompra(Integer numeroOrdenCompra) {
		this.numeroOrdenCompra = numeroOrdenCompra;
	}
	
}

package com.sodimac.rebates.dto;

import java.util.List;

public class ProveedorDto {

	private String numProveedor;
	private String nomProveedor;
	private List<OrdenCompraDto> ordenesCompra;

	public String getNumProveedor() {
		return numProveedor;
	}

	public void setNumProveedor(String numProveedor) {
		this.numProveedor = numProveedor;
	}

	public String getNomProveedor() {
		return nomProveedor;
	}

	public void setNomProveedor(String nomProveedor) {
		this.nomProveedor = nomProveedor;
	}

	public List<OrdenCompraDto> getOrdenesCompra() {
		return ordenesCompra;
	}

	public void setOrdenesCompra(List<OrdenCompraDto> ordenesCompra) {
		this.ordenesCompra = ordenesCompra;
	}

}

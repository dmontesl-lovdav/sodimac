package com.sodimac.rebates.dto;

public class ExclusionCargaDetDto {

	private Integer idExclusionCarga;
	private Long idExclusionCargaDet;
	private String motivo;
	private String numProveedor;
	private String nomProveedor;
	private String ordenCompra;
	private String clacom;
	private String sku;
	private String skuDescripcion;
	private boolean activo;
	private String cantidadOrdenada;
	private String cantidadRecibida;

	public Integer getIdExclusionCarga() {
		return idExclusionCarga;
	}

	public void setIdExclusionCarga(Integer idExclusionCarga) {
		this.idExclusionCarga = idExclusionCarga;
	}

	public Long getIdExclusionCargaDet() {
		return idExclusionCargaDet;
	}

	public void setIdExclusionCargaDet(Long idExclusionCargaDet) {
		this.idExclusionCargaDet = idExclusionCargaDet;
	}

	public String getMotivo() {
		return motivo;
	}

	public void setMotivo(String motivo) {
		this.motivo = motivo;
	}

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

	public String getOrdenCompra() {
		return ordenCompra;
	}

	public void setOrdenCompra(String ordenCompra) {
		this.ordenCompra = ordenCompra;
	}

	public String getClacom() {
		return clacom;
	}

	public void setClacom(String clacom) {
		this.clacom = clacom;
	}

	public String getSku() {
		return sku;
	}

	public void setSku(String sku) {
		this.sku = sku;
	}

	public String getSkuDescripcion() {
		return skuDescripcion;
	}

	public void setSkuDescripcion(String skuDescripcion) {
		this.skuDescripcion = skuDescripcion;
	}

	public boolean isActivo() {
		return activo;
	}

	public void setActivo(boolean activo) {
		this.activo = activo;
	}

	public String getCantidadOrdenada() {
		return cantidadOrdenada;
	}

	public void setCantidadOrdenada(String cantidadOrdenada) {
		this.cantidadOrdenada = cantidadOrdenada;
	}

	public String getCantidadRecibida() {
		return cantidadRecibida;
	}

	public void setCantidadRecibida(String cantidadRecibida) {
		this.cantidadRecibida = cantidadRecibida;
	}

}

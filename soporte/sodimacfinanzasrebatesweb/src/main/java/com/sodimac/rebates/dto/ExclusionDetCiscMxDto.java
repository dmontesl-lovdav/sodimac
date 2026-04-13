package com.sodimac.rebates.dto;

import java.util.List;

public class ExclusionDetCiscMxDto {

	private String jsonId;
	private String clacom;
	private RebateProveedorDto proveedor;
	private String ordenCompra;
	private ExclusionCargaDto exclusionCarga;
	private List<RebatesCicmxOcDto> cicmxOcDtos;
	private List<ExclusionCargaDetDto> listExclusionCargaDet;
	
	public String getJsonId() {
		return jsonId;
	}

	public void setJsonId(String jsonId) {
		this.jsonId = jsonId;
	}

	public String getClacom() {
		return clacom;
	}

	public void setClacom(String clacom) {
		this.clacom = clacom;
	}	
	
	public RebateProveedorDto getProveedor() {
		return proveedor;
	}

	public void setProveedor(RebateProveedorDto proveedor) {
		this.proveedor = proveedor;
	}

	public String getOrdenCompra() {
		return ordenCompra;
	}

	public void setOrdenCompra(String ordenCompra) {
		this.ordenCompra = ordenCompra;
	}

	public ExclusionCargaDto getExclusionCarga() {
		return exclusionCarga;
	}

	public void setExclusionCarga(ExclusionCargaDto exclusionCarga) {
		this.exclusionCarga = exclusionCarga;
	}

	public List<RebatesCicmxOcDto> getCicmxOcDtos() {
		return cicmxOcDtos;
	}

	public void setCicmxOcDtos(List<RebatesCicmxOcDto> cicmxOcDtos) {
		this.cicmxOcDtos = cicmxOcDtos;
	}

	public List<ExclusionCargaDetDto> getListExclusionCargaDet() {
		return listExclusionCargaDet;
	}

	public void setListExclusionCargaDet(List<ExclusionCargaDetDto> listExclusionCargaDet) {
		this.listExclusionCargaDet = listExclusionCargaDet;
	}
}

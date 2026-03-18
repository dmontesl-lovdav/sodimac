package com.sodimac.wsconfiguracion.dto;

public class EmisorYLugarExpedicionDto {
	

	private ComprobanteDto comprobanteNode;

	public EmisorYLugarExpedicionDto () {}
	
	public EmisorYLugarExpedicionDto (ComprobanteDto comprobante) {
		this.comprobanteNode = comprobante;
		
	}
	

	public ComprobanteDto getComprobanteNode() {
		return comprobanteNode;
	}

	public void setComprobanteNode(ComprobanteDto comprobanteNode) {
		this.comprobanteNode = comprobanteNode;
	}


	
	

}

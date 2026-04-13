package com.sodimac.rebates.enums;

public enum ESeguridad {
	DOCUMENTOS_MENU("/documents/index","DOCUMENTOS_INDEX"),
	FILLRATE_MENU("/fillrate/index","FILLRATE_INDEX"),
	AUTHORIZATION_MENU("/authorization/index","AUTHORIZATION_INDEX"),
	ORDER_COMPRA_MENU("/ordenCompra/index","ORDER_COMPRA_INDEX"),
	ORDER_COMPRA_FILL_MENU("/ordenCompraFill/index","ORDER_COMPRA_FILL_INDEX"),
	REBASTES_MSI_MENU("/rebatesMSI/index","REBASTES_MSI_INDEX"),
	REBATES_MSI3_MENU("/rebatesMSI3/index","REBATES_MSI3_INDEX"),
	REBATES_USUARIO_MENU("/rebateUsuario/index","REBATES_USUARIO_INDEX"),
	REBATES_USUARIO_FILLRATE_MENU("/usuarioFillRate/index","REBATES_USUARIO_FILLRATE_INDEX"),
	ACUERDOS_MENU("/acuerdos/index","ACUERDOS_INDEX"),
	EXCLUSIONES_MENU("/exclusiones/index","EXCLUSIONES_INDEX"),
	CATALOGOS_MENU("/catalogos/index","CATALOGOS_INDEX"),
	POLIZAS_MENU("/polizas/index","POLIZAS_INDEX"),
	REPORTE_FINANCIERO_MENU("/reporteFinanciero/index","REPORTE_FINANCIERO_INDEX");
	
	private String url;
	private String authority;
	
	ESeguridad(String url, String authority) {
		this.url = url;
		this.authority = authority;
	}

	public String getUrl() {
		return url;
	}

	public void setUrl(String url) {
		this.url = url;
	}

	public String getAuthority() {
		return authority;
	}

	public void setAuthority(String authority) {
		this.authority = authority;
	}
}

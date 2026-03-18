package com.sodimac.facturacion.service.ws;

import com.sodimac.facturacion.clientews.wcfemision.org.datacontract.schemas._2004._07.cfdiWcfEmisionServicio_Sodimac_Clases.RespuestaXml;
import com.sodimac.facturacion.clientews.wcfemision.org.datacontract.schemas._2004._07.cfdiWcfEmisionServicio_Sodimac_Clases.Resultado;

public interface EmisionService {

	public Resultado timbrar(String xml);
	public RespuestaXml getComprobante(String facturaId);
	public Resultado getComprobantePdf(String facturaId);
	public Resultado cancelar(String facturaId);
	public Resultado timbrar(String xml, String tipoTimbrado);
	public RespuestaXml getComprobante(String facturaId, String tipoTimbrado);
}

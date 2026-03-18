package com.sodimac.facturacion.service.ws;

import com.sodimac.facturacion.clientews.wcfemision40.org.datacontract.schemas._2004._07.cfdiwcfemisionservicio40_sodimac.RespuestaXml;
import com.sodimac.facturacion.clientews.wcfemision40.org.datacontract.schemas._2004._07.cfdiwcfemisionservicio40_sodimac.Resultado;

public interface Emision40Service {

	public Resultado timbrar(String xml);
	public RespuestaXml getComprobante(String facturaId);
	public Resultado getComprobantePdf(String facturaId);
	public Resultado cancelar(String facturaId);
	public Resultado timbrar(String xml, String tipoTimbrado);
	public RespuestaXml getComprobante(String facturaId, String tipoTimbrado);
}

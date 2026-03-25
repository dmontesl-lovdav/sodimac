package com.sodimac.facturacion.util;

import java.io.File;
import java.io.UnsupportedEncodingException;
import java.math.BigInteger;
import java.net.URLDecoder;

import com.falabella.mdwcorp.sod.corp.osb.schema.cliente.factura.confirmar.resp_v2018.ClienteFacturaConfirmarExpRespTYPE;
import com.falabella.mdwcorp.sod.corp.osb.schema.cliente.factura.confirmar.resp_v2018.ResponseTYPE;
import com.falabella.mdwcorp.sod.corp.osb.schema.cliente.ticket.obtener.resp_v2018.ClienteTicketObtenerExpRespTYPE;
import com.falabella.mdwcorp.sod.corp.osb.schema.cliente.ticket.obtener.resp_v2018.ClienteTicketObtenerExpRespTYPE.Respuesta;;

public class UtilsApi {
	
	public static void setRespuesta(ClienteTicketObtenerExpRespTYPE response, int codigo, String descripcion) {
    	Respuesta respuesta = new Respuesta();
    	respuesta.setCodigo(Integer.toString(codigo));
    	respuesta.setDescripcion(descripcion);
    	response.setRespuesta(respuesta);
    }
		
	public static void setRespuesta(ClienteFacturaConfirmarExpRespTYPE response, int codigo, String descripcion) {
		BigInteger codigoRespuesta = BigInteger.valueOf(codigo);
		ResponseTYPE respuesta = new ResponseTYPE();
		respuesta.setCodigoRespuesta(codigoRespuesta);
		respuesta.setMensajeRespuesta(descripcion);
		response.setResponse(respuesta);
    }

	public static String getPathCifradoProperties() {
		String resourceName = "cifrado.properties";
		String absolutePath = "";
		
		File file = new File(UtilsApi.class.getClassLoader().getResource(resourceName).getFile());
		try {
			absolutePath = URLDecoder.decode(file.getAbsolutePath(), "UTF-8");
		} catch (UnsupportedEncodingException e) {
			e.printStackTrace();
		}
		
		return absolutePath;
	}
	
}

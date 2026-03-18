package com.sodimac.facturacion.util;

import java.util.HashMap;

import com.sodimac.facturacion.clientews.model.ClientResponseTYPE;
import com.sodimac.facturacion.clientews.model.RespuestaClient;
import com.sodimac.facturacion.clientews.wsft.ClienteTicketTimbrarExpRespTYPE;
import com.sodimac.facturacion.clientews.wsft.ClienteTicketTimbrarExpRespTYPE.Respuesta;

public class UtilsApi {
	
	static HashMap<Integer, String> codigos = new HashMap<>();
		
	public static void inicializa() {
		if (codigos.isEmpty()) {
			codigos.put(0, "La solicitud es inv\u00e1lida u ocurri\u00f3 un error");
			codigos.put(1, "OK");
			codigos.put(5, "Acceso denegado, usuario/token no autorizado");
			codigos.put(105, "Estructura de documento inv\u00e1lido");
			codigos.put(112, "Fecha invalida");
		}

	}
    
	public static void setRespuesta(ClienteTicketTimbrarExpRespTYPE response, int respuestaCodigo) {
		inicializa();
    	Respuesta respuesta = new Respuesta();
    	respuesta.setCodigo(Integer.toString(respuestaCodigo));
    	respuesta.setDescripcion(codigos.get(respuestaCodigo));
    	response.setRespuesta(respuesta);
    	
    }
	
	public static void setRespuesta(ClienteTicketTimbrarExpRespTYPE response, int respuestaCodigo, String descripcion) {
		inicializa();
    	Respuesta respuesta = new Respuesta();
    	respuesta.setCodigo(Integer.toString(respuestaCodigo));
    	respuesta.setDescripcion(descripcion);
    	response.setRespuesta(respuesta);
    	
    }

	public static void setRespuesta(ClientResponseTYPE<?> response, int respuestaCodigo) {
		inicializa();
		RespuestaClient respuesta = new RespuestaClient();
    	respuesta.setCodigo(Integer.toString(respuestaCodigo));
    	respuesta.setDescripcion(codigos.get(respuestaCodigo));
    	response.setRespuesta(respuesta);
	}

}

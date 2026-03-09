package com.sodimac.cfdi.util;

import java.io.File;
import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.util.HashMap;

import com.sodimac.cfdi.clientews.wsft.ClienteTicketTimbrarExpRespTYPE;
import com.sodimac.cfdi.clientews.wsft.ClienteTicketTimbrarExpRespTYPE.Respuesta;;

public class UtilsApi {
	
	static HashMap<Integer, String> codigos = new HashMap<>();
		
	public static void inicializa() {
		if (codigos.isEmpty()) {
			codigos.put(0, "La solicitud es inválida u ocurrió un error");
			codigos.put(1, "OK");
			codigos.put(5, "Acceso denegado, usuario/token no autorizado");
			codigos.put(105, "Estructura de documento inválido");
			codigos.put(112, "La fecha es inválida");			
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

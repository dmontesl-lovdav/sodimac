package com.sodimac.wsconfiguracion.util;

import java.io.File;
import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.util.HashMap;

import com.sodimac.wsconfiguracion.dto.ResponseBaseDto;
//import com.sodimac.wsconfiguracion.models.ClientesTemporalModel;
//import com.sodimac.wsconfiguracion.repository.fac.QueryFacRepository;
import com.sodimac.wsconfiguracion.util.enums.ECodigo;

public class UtilsApi {
	
	public static HashMap<Integer, String> codigos = new HashMap<>();
		
	public static void inicializa() {
		if (codigos.isEmpty()) {
			codigos.put(0, "La solicitud es inválida u ocurrió un error");
			codigos.put(1, "OK");
			codigos.put(5, "Acceso denegado, usuario/token no autorizado");
			codigos.put(105, "Estructura de documento inválido");
			codigos.put(112, "Fecha invalida");
		}

	}
    
	public static void setRespuesta(ResponseBaseDto response, ECodigo ec) {
		response.setCodigo(Integer.toString(ec.getValor()));
		response.setDescripcion(ec.message());
	}
	
	public static void setRespuesta(ResponseBaseDto response, ECodigo ec, String message) {
		response.setCodigo(Integer.toString(ec.getValor()));
		response.setDescripcion(ec.message() + " " + message);
	}
	
//	public static void setRespuesta(ClienteTicketTimbrarExpRespTYPE response, int respuestaCodigo) {
//		inicializa();
//    	Respuesta respuesta = new Respuesta();
//    	respuesta.setCodigo(Integer.toString(respuestaCodigo));
//    	respuesta.setDescripcion(codigos.get(respuestaCodigo));
//    	response.setRespuesta(respuesta);
//    	
//    }
//	
//	public static void setRespuesta(ClienteTicketTimbrarExpRespTYPE response, int respuestaCodigo, String descripcion) {
//		inicializa();
//    	Respuesta respuesta = new Respuesta();
//    	respuesta.setCodigo(Integer.toString(respuestaCodigo));
//    	respuesta.setDescripcion(descripcion);
//    	response.setRespuesta(respuesta);
//    	
//    }
//		
//	public static void setRespuesta(ClienteTicketTimbrarExpRespTYPE response, int respuestaCodigo, ClientesTemporalModel model) {
//		inicializa();
//		setRespuestaFacturacion(response, respuestaCodigo);
//		String mensaje = response.getRespuesta().getDescripcion().replace("{ticket}", model.getTicket()).replace("{rfc}", model.getRfc());
//		setRespuesta(response, respuestaCodigo, mensaje);        		        		
//    	
//    }
//	
//	public static void setRespuestaFacturacion(ClienteTicketTimbrarExpRespTYPE response, int respuestaCodigo) {
//		inicializa();
//		
//		String sql = "select DescripcionMensaje from catmensajes where idMensaje=" + Integer.toString(respuestaCodigo);
//		String descripcion = (String) QueryFacRepository.executeGetSingleResult(sql);
// 
//    	Respuesta respuesta = new Respuesta();
//    	respuesta.setCodigo(Integer.toString(respuestaCodigo));
//    	respuesta.setDescripcion(descripcion);
//    	response.setRespuesta(respuesta);
//    	
//    }

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

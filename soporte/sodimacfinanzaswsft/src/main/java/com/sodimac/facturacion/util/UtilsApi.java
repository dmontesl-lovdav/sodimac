package com.sodimac.facturacion.util;

import java.io.File;
import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.util.HashMap;

import com.falabella.mdwcorp.sod.corp.osb.schema.cliente.ticket.obtener.resp_v2022.ClienteTicketObtenerExpRespTYPE;
import com.sodimac.facturacion.cliente.ClienteObtenerDetalleTicketExpRespTYPE;
import com.sodimac.facturacion.cliente.ClienteTicketTimbrarExpRespTYPE;
import com.sodimac.facturacion.cliente.ClienteTicketTimbrarExpRespTYPE.Respuesta;
import com.sodimac.facturacion.models.ClientesTemporalModel;
import com.sodimac.facturacion.models.CompTemporalModel;
import com.sodimac.facturacion.repository.fac.QueryFacRepository;
import com.sodimac.facturacion.util.enums.ECodigo;

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
		
	public static void setRespuesta(ClienteTicketTimbrarExpRespTYPE response, int respuestaCodigo, ClientesTemporalModel model) {
		inicializa();
		setRespuestaFacturacion(response, respuestaCodigo);
		String mensaje = response.getRespuesta().getDescripcion().replace("{ticket}", model.getTicket()).replace("{rfc}", model.getRfc());
		setRespuesta(response, respuestaCodigo, mensaje);        		        		
    	
    }
	
	public static void setRespuestaFacturacion(ClienteTicketTimbrarExpRespTYPE response, int respuestaCodigo) {
		inicializa();
		
		String sql = "select DescripcionMensaje from catmensajes where idMensaje=" + Integer.toString(respuestaCodigo);
		String descripcion = (String) QueryFacRepository.executeGetSingleResult(sql);
 
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
	
	public static void setRespuesta(ClienteObtenerDetalleTicketExpRespTYPE response) {
		ClienteObtenerDetalleTicketExpRespTYPE.Respuesta respuesta = new ClienteObtenerDetalleTicketExpRespTYPE.Respuesta();
		respuesta.setCodigo(Integer.toString(ECodigo.Ok.getValor()));
    	respuesta.setDescripcion("Ok");
    	response.setRespuesta(respuesta);
    	ClienteTicketObtenerExpRespTYPE responseWsObtenerTicket99 = new ClienteTicketObtenerExpRespTYPE();
    	com.falabella.mdwcorp.sod.corp.osb.schema.cliente.ticket.obtener.resp_v2022.ClienteTicketObtenerExpRespTYPE.Respuesta respuesta99 = new com.falabella.mdwcorp.sod.corp.osb.schema.cliente.ticket.obtener.resp_v2022.ClienteTicketObtenerExpRespTYPE.Respuesta();
    	respuesta99.setCodigo("99");
    	respuesta99.setDescripcion("Ocurrio un error durante la generacion del comprobante");
		responseWsObtenerTicket99.setRespuesta(respuesta99);
		response.setResponseWSObtenerTicket(responseWsObtenerTicket99);
    	
    }
	
	public static void setRespuesta(ClienteTicketTimbrarExpRespTYPE response, int respuestaCodigo, CompTemporalModel model) {
		inicializa();
		setRespuestaFacturacion(response, respuestaCodigo);
		String mensaje = response.getRespuesta().getDescripcion().replace("{ticket}", model.getTicket()).replace("{rfc}", model.getRfc());
		setRespuesta(response, respuestaCodigo, mensaje);        		        		
    	
    }

}

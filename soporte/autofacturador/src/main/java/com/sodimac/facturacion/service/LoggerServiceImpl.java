package com.sodimac.facturacion.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.falabella.mdwcorp.sod.corp.osb.schema.cliente.factura.confirmar.resp_v2018.ClienteFacturaConfirmarExpRespTYPE;
import com.falabella.mdwcorp.sod.corp.osb.schema.cliente.ticket.obtener.resp_v2018.ClienteTicketObtenerExpRespTYPE;
import com.sodimac.facturacion.models.ClientesTemporalModel;
import com.sodimac.facturacion.repository.LogErroresRepository;

@Service
public class LoggerServiceImpl implements LoggerService {
	
	@Autowired
	private LogErroresRepository logErroresRepository;
	@Autowired
	private SeguridadService seguridadService;
	
	private String longitud="0";
	private String latitud="0";
	private String pagina="";
	private String explorador="";
	private String sistemaOperativo="";
	private String ip="";
	private String ticket="";
	private String rfc="";
	private String xml="";
	private int idFacturaPac=0;
	private String sessionId="";

	public void setLogErroresProperties (
			  String longitud
			, String latitud
			, String explorador
			, String sistemaOper
			, String ip
			, String ticket
			, String rfc
			) {
		this.longitud = longitud;
		this.latitud = latitud;
		this.explorador = explorador;
		this.sistemaOperativo = sistemaOper;
		this.ip = ip;
		this.ticket = ticket;
		this.rfc = rfc;
	}
	
	@Transactional
	public void write (String error, String objeto, String params) {
		
		if (!error.trim().isEmpty()) {
			logErroresRepository.registrarErrorV2(error.replace(rfc, seguridadService.encriptar(rfc)), objeto, params, 0, longitud, latitud, pagina, explorador, sistemaOperativo, ip, ticket, seguridadService.encriptar(rfc), seguridadService.encriptar(xml), idFacturaPac, sessionId);
		}

	}
	
	@Override
	@Transactional
	public void guardarLog(Object obj, ClientesTemporalModel model, String longitud, String latitud, String explorador,
			String sistemaOper, String ip, String ticket, String rfc, String pagina, String xml, int idFacturaPac, String sessionId) {
		
		this.longitud = longitud;
		this.latitud = latitud;
		this.explorador = explorador;
		this.sistemaOperativo = sistemaOper;
		this.ip = ip;
		this.ticket = ticket;
		this.rfc = rfc;
		this.pagina = pagina;
		this.xml = xml;
		this.idFacturaPac = idFacturaPac;
		this.sessionId = sessionId;
		
		String objeto = obj.getClass().getName();
		String error = "";
		String params = "";
		
		if (obj.getClass().equals(String.class)) {
			error = (String) obj;
			params = "";
		}
		
		if (obj.getClass().getName().endsWith("ClienteFacturaConfirmarExpRespTYPE")) {
			ClienteFacturaConfirmarExpRespTYPE result = (ClienteFacturaConfirmarExpRespTYPE) obj;
			error = "Codigo: " + result.getResponse().getCodigoRespuesta()
			+ ", Descripcion: " + result.getResponse().getMensajeRespuesta()
			+ ", Ticket: " + model.getTicket()
			+ "";
			params = "WsConfirmar";
		}
		if (obj.getClass().getName().endsWith("ClienteTicketObtenerExpRespTYPE")) {
			ClienteTicketObtenerExpRespTYPE result = (ClienteTicketObtenerExpRespTYPE) obj;
			error = "Codigo: " + result.getRespuesta().getCodigo()
			+ ", Descripcion: " + result.getRespuesta().getDescripcion()
			+ ", Ticket: " + model.getTicket()
			+ "";
			params = "WsTicket";
		}
		if (obj.getClass().getName().endsWith("com.sun.xml.internal.ws.client.ClientTransportException")) {
			objeto = obj.getClass().getName();
			Exception result = (Exception) obj;
			error = "ErrorMessage: " + result.getMessage()
			+ ", Cause: " + result.getCause()
			+ ", StackTrace: " + result.getStackTrace()
			+ ", Ticket: " + model.getTicket()
			+ "";
			params = "WsTicket Exception";
		}
		if (obj.getClass().getName().endsWith("Exception")) {
			objeto = obj.getClass().getName();
			Exception result = (Exception) obj;
			error = "ErrorMessage: " + result.getMessage()
			+ ", Cause: " + result.getCause()
			+ ", StackTrace: " + result.getStackTrace()
			+ ", Ticket: " + model.getTicket()
			+ "";
			params = "Exception";
		}
		write(error, objeto, params);
		
	}
	
	@Override
	@Transactional
	public void guardarLog(Object obj, String longitud, String latitud, String explorador, String sistemaOper,
			String ip, String ticket, String rfc, String pagina, String xml, int idFacturaPac, String sessionId) {
		String objeto = obj.getClass().getName();
		String error = "";
		String params = "";
		
		this.longitud = longitud;
		this.latitud = latitud;
		this.explorador = explorador;
		this.sistemaOperativo = sistemaOper;
		this.ip = ip;
		this.ticket = ticket;
		this.rfc = rfc;
		this.pagina = pagina;
		this.xml = xml;
		this.idFacturaPac = idFacturaPac;
		this.sessionId = sessionId;
		
		if (obj.getClass().equals(String.class)) {
			error = (String) obj;
			params = "";
		}
		
		if (obj.getClass().getName().endsWith("Exception")) {
			Exception result = (Exception) obj;
			error = "ErrorMessage: " + result.getMessage()
			+ ", Cause: " + result.getCause()
			+ ", StackTrace: " + result.getStackTrace()
			+ "";
			params = "Exception";
		}
		
		write(error, objeto, params);// TODO Auto-generated method stub
		
	}


}

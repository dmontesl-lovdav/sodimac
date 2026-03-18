package com.sodimac.wsconfiguracion.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

//import com.falabella.mdwcorp.sod.corp.osb.schema.cliente.factura.confirmar.resp_v2018.ClienteFacturaConfirmarExpRespTYPE;
//import com.falabella.mdwcorp.sod.corp.osb.schema.cliente.ticket.obtener.resp_v2018.ClienteTicketObtenerExpRespTYPE;
//import com.sodimac.wsconfiguracion.clientews.wcfemision.org.datacontract.schemas._2004._07.cfdiWcfEmisionServicio_Sodimac_Clases.RespuestaXml;
//import com.sodimac.wsconfiguracion.clientews.wcfemision.org.datacontract.schemas._2004._07.cfdiWcfEmisionServicio_Sodimac_Clases.Resultado;
import com.sodimac.wsconfiguracion.entity.config.LogErroresEntity;
import com.sodimac.wsconfiguracion.repository.config.LogErroresRepository;

@Service
public class LoggerServiceImpl implements LoggerService {
	
	@Autowired
	@Qualifier("logErroresRepositoryConfig")
	private LogErroresRepository logErroresRepository;
	@Autowired
	private SeguridadService seguridadService;
	
	private String longitud="0";
	private String latitud="0";
	private String pagina="";
	private String explorador="";
	private String sistemaOperativo="";
	private String direccionip ="";
	private String ticket="";
	private String rfc="";
	private String xml="";
	private int idFacturaPac=0;
	private String sessionId = "";
	private String xmlRequest = "";
	private String xmlResponse = "";
	private String parametrosLlamado = "";

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
		this.direccionip  = ip;
		this.ticket = ticket;
		this.rfc = rfc;
	}
	
	@Transactional
	public void write (String error, String objeto, String params) {
		
		LogErroresEntity logErroresEntity = new LogErroresEntity();
		logErroresEntity.setErrorLog(error);
		logErroresEntity.setTicket(ticket);
		logErroresEntity.setXmlRequest(xmlRequest);
		logErroresEntity.setXmlResponse(xmlResponse);
		logErroresEntity.setObjeto(objeto);
		logErroresEntity.setExplorador(explorador);
		logErroresEntity.setSistemaOperativo(sistemaOperativo);
		logErroresEntity.setDireccionip(direccionip);
		logErroresEntity.setParametrosLlamado(params + ". Parametros de Llamado: " + parametrosLlamado);
		if (!error.trim().isEmpty()) {
			logErroresRepository.save(logErroresEntity);
		}

	}
	
//	@Override
//	@Transactional
//	public void guardarLog(Object obj, ClientesTemporalModel model, String longitud, String latitud, String explorador,
//			String sistemaOper, String ip, String ticket, String rfc, String pagina, String xml, int idFacturaPac, String sessionId, String parametrosLlamado) {
//		
//		this.longitud = longitud;
//		this.latitud = latitud;
//		this.explorador = explorador;
//		this.sistemaOperativo = sistemaOper;
//		this.direccionip = ip;
//		this.ticket = ticket;
//		this.rfc = rfc;
//		this.pagina = pagina;
//		this.xml = xml;
//		this.idFacturaPac = idFacturaPac;
//		this.sessionId = sessionId;
//		this.parametrosLlamado = parametrosLlamado;
//		
//		String objeto = obj.getClass().getName();
//		String error = "";
//		String params = "";
//		
//		if (obj.getClass().equals(String.class)) {
//			error = (String) obj;
//			params = "";
//		}
//		
////		if (obj.getClass().getName().endsWith("ClienteFacturaConfirmarExpRespTYPE")) {
////			ClienteFacturaConfirmarExpRespTYPE result = (ClienteFacturaConfirmarExpRespTYPE) obj;
////			error = "Codigo: " + result.getResponse().getCodigoRespuesta()
////			+ ", Descripcion: " + result.getResponse().getMensajeRespuesta()
////			+ ", Ticket: " + model.getTicket()
////			+ "";
////			params = "WsConfirmar";
////		}
////		if (obj.getClass().getName().endsWith("ClienteTicketObtenerExpRespTYPE")) {
////			ClienteTicketObtenerExpRespTYPE result = (ClienteTicketObtenerExpRespTYPE) obj;
////			error = "Codigo: " + result.getRespuesta().getCodigo()
////			+ ", Descripcion: " + result.getRespuesta().getDescripcion()
////			+ ", Ticket: " + model.getTicket()
////			+ "";
////			params = "WsTicket";
////		}
////		if (obj.getClass().getName().endsWith("RespuestaXml")) {
////			RespuestaXml result = (RespuestaXml) obj;
////			String estatusId = result.getEstatusId() == null? "":result.getEstatusId();
////			String errorMessage = result.getErrorMessage() == null? "":result.getErrorMessage();
////			String errorDesc = result.getErrorDesc() == null? "":result.getErrorDesc();
////			String folio = result.getFolio() == null? "":result.getFolio();
////			String facturaId = result.getFacturaId() == null? "":result.getFacturaId();
////			error = "EstatusId: " + estatusId
////			+ ", ErrorMessage: " + errorMessage
////			+ ", ErrorDesc: " + errorDesc
////			+ ", Ticket: " + model.getTicket()
////			+ ", Folio: " + folio
////			+ ", FacturaId: " + facturaId
////			+ "";
////			params = "WsEmision";
////		}
//		if (obj.getClass().getName().endsWith("com.sun.xml.internal.ws.client.ClientTransportException")) {
//			objeto = obj.getClass().getName();
//			Exception result = (Exception) obj;
//			error = "ErrorMessage: " + result.getMessage()
//			+ ", Cause: " + result.getCause()
//			+ ", StackTrace: " + result.getStackTrace()
//			+ ", Ticket: " + model.getTicket()
//			+ "";
//			params = "WsTicket Exception";
//		}
//		if (obj.getClass().getName().endsWith("Exception")) {
//			objeto = obj.getClass().getName();
//			Exception result = (Exception) obj;
//			error = "ErrorMessage: " + result.getMessage()
//			+ ", Cause: " + result.getCause()
//			+ ", StackTrace: " + result.getStackTrace()
//			+ ", Ticket: " + model.getTicket()
//			+ "";
//			params = "Exception";
//		}
//		write(error, objeto, params);
//		
//	}
	
	@Override
	@Transactional
	public void guardarLog(Object obj, String longitud, String latitud, String explorador, String sistemaOper,
			String ip, String ticket, String rfc, String pagina, String xml, int idFacturaPac, String sessionId, String parametrosLlamado) {
		String objeto = obj.getClass().getName();
		String error = "";
		String params = "";
		
		this.longitud = longitud;
		this.latitud = latitud;
		this.explorador = explorador;
		this.sistemaOperativo = sistemaOper;
		this.direccionip = ip;
		this.ticket = ticket;
		this.rfc = rfc;
		this.pagina = pagina;
		this.xml = xml;
		this.idFacturaPac = idFacturaPac;
		this.sessionId = sessionId;
		this.parametrosLlamado = parametrosLlamado;
		
		if (obj.getClass().equals(String.class)) {
			error = (String) obj;
			params = "";
		}
		
//		if (obj.getClass().getName().endsWith("cfdiWcfEmisionServicio_Sodimac_Clases.Resultado")) {
//			Resultado result = (Resultado) obj;
//			error = "ErrorMessage: " + result.getErrorMessage()
//			+ "";
//			params = "";
//		}
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

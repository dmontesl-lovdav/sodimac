package com.sodimac.cfdi.component;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.sodimac.cfdi.models.ClientesTemporalModel;
import com.sodimac.cfdi.service.LoggerService;

@Component
public class ErrorComponent {
	
	@Autowired
	private LoggerService loggerService;
	
	private int idActividad;
	private String actividadDesc = "";
	private String ticket = "";
	private int usuario = 0;
	private String longitud = "0";
	private String latitud = "0";
	private String pagina = "";
	private String explorador = "";
	private String sistemaOper = "";
	private String ip = "";
	private String sessionId = "";
	private String rfc = "";
	private String xml = "";
	private int idFacturaPac = 0;
	private String descargar = "";
	
	public void setErrorProperties (
			  String longitud
			, String latitud
			, String explorador
			, String sistemaOper
			, String ip
			, String rfc
			, String sessionId
			) {
		this.longitud = longitud;
		this.latitud = latitud;
		this.explorador = explorador;
		this.sistemaOper = sistemaOper;
		this.ip = ip;
		this.rfc = rfc;
		this.sessionId = sessionId;
	}
	
	public void guardarLog (Object obj, ClientesTemporalModel model ) {
		loggerService.guardarLog(obj, model, longitud, latitud, explorador, sistemaOper, ip, ticket, rfc, pagina, xml, idFacturaPac, sessionId);
	}
	
	public void guardarLog (Object obj) {
		loggerService.guardarLog(obj, longitud, latitud, explorador, sistemaOper, ip, ticket, rfc, pagina, xml, idFacturaPac, sessionId);
	}
	
	public int getIdActividad() {
		return idActividad;
	}
	public void setIdActividad(int idActividad) {
		this.idActividad = idActividad;
	}
	public String getActividadDesc() {
		return actividadDesc;
	}
	public void setActividadDesc(String actividadDesc) {
		this.actividadDesc = actividadDesc;
	}
	public String getTicket() {
		return ticket;
	}
	public void setTicket(String ticket) {
		this.ticket = ticket;
	}
	public int getUsuario() {
		return usuario;
	}
	public void setUsuario(int usuario) {
		this.usuario = usuario;
	}
	public String getLongitud() {
		return longitud;
	}
	public void setLongitud(String longitud) {
		this.longitud = longitud;
	}
	public String getLatitud() {
		return latitud;
	}
	public void setLatitud(String latitud) {
		this.latitud = latitud;
	}
	public String getPagina() {
		return pagina;
	}
	public void setPagina(String pagina) {
		this.pagina = pagina;
	}
	public String getExplorador() {
		return explorador;
	}
	public void setExplorador(String explorador) {
		this.explorador = explorador;
	}
	public String getSistemaOper() {
		return sistemaOper;
	}
	public void setSistemaOper(String sistemaOper) {
		this.sistemaOper = sistemaOper;
	}
	public String getIp() {
		return ip;
	}
	public void setIp(String ip) {
		this.ip = ip;
	}
	public String getSessionId() {
		return sessionId;
	}
	public void setSessionId(String sessionId) {
		this.sessionId = sessionId;
	}
	public String getRfc() {
		return rfc;
	}
	public void setRfc(String rfc) {
		this.rfc = rfc;
	}

	public String getXml() {
		return xml;
	}

	public void setXml(String xml) {
		this.xml = xml;
	}

	public int getIdFacturaPac() {
		return idFacturaPac;
	}

	public void setIdFacturaPac(int idFacturaPac) {
		this.idFacturaPac = idFacturaPac;
	}

	public String getDescargar() {
		return descargar;
	}

	public void setDescargar(String descargar) {
		this.descargar = descargar;
	}

	
}

package com.sodimac.facturacion.component;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.web.context.annotation.SessionScope;

import com.sodimac.facturacion.entity.CatActividadesEntity;
import com.sodimac.facturacion.service.CatActividadesService;
import com.sodimac.facturacion.util.UtilsString;

@Component
@SessionScope
public class ActividadesComponent {

	@Autowired
	private CatActividadesService catActividadesService;

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
	private String token = "";

	public void setActividadesProperties (
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


	public int registrarActividad(int idActividad, List<String> actividadDesc, String pagina) {
		this.idActividad = idActividad;
		this.pagina = pagina;
		this.actividadDesc = UtilsString.parseActivityDesc(idActividad, actividadDesc, getActividad(idActividad));

		return catActividadesService.registrarActividad(
				this.idActividad, 
				this.actividadDesc, 
				this.ticket, 
				this.usuario, 
				this.longitud, 
				this.latitud,
				this.pagina,
				this.explorador, 
				this.sistemaOper, 
				this.ip, 
				this.rfc, 
				this.sessionId
				);
		
	}

	public CatActividadesEntity getActividad(int idActividad) {

		return catActividadesService.getActividad(idActividad);
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

	public String getToken() {
		return token;
	}

	public void setToken(String token) {
		this.token = token;
	}

}

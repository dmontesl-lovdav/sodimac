package com.sodimac.wsconfiguracion.entity.config;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Lob;
import javax.persistence.Table;
import javax.persistence.Temporal;
import javax.persistence.TemporalType;

import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.Type;

@Entity
@Table(name = "logerrores")
public class LogErroresEntity {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	@Column(name = "idLogError")
	private int idLogError;

	@Column(name = "fechaCreacion")
	@CreationTimestamp
	@Temporal(TemporalType.TIMESTAMP)
	private java.util.Date fechaCreacion;

	@Lob
	@Column(name = "errorLog")
	private String errorLog;
	
	@Column(name = "ticket")
	private String ticket;
	
	@Column(name = "sessionId")
	private String sessionId;
	
	@Column(name = "xmlRequest")
	private String xmlRequest;
	
	@Column(name = "xmlResponse")
	private String xmlResponse;

	@Column(name = "idUsuario", nullable=true)
	private Integer idUsuario;

	@Column(name = "objeto")
	private String objeto;

	@Column(name = "longitud")
	private String longitud;

	@Column(name = "latitud")
	private String latitud;

	@Column(name = "pagina")
	private String pagina;

	@Column(name = "explorador")
	private String explorador;

	@Column(name = "sistemaOperativo")
	private String sistemaOperativo;

	@Column(name = "direccionip")
	private String direccionip;

	@Column(name = "parametrosLlamado")
	private String parametrosLlamado;

	@Column(name = "activo", nullable = false, columnDefinition = "BIT", length = 1)
	@Type(type = "org.hibernate.type.NumericBooleanType")
	private boolean activo = true;
	

	public LogErroresEntity() {

	}


	public int getIdLogError() {
		return idLogError;
	}


	public void setIdLogError(int idLogError) {
		this.idLogError = idLogError;
	}


	public java.util.Date getFechaCreacion() {
		return fechaCreacion;
	}


	public void setFechaCreacion(java.util.Date fechaCreacion) {
		this.fechaCreacion = fechaCreacion;
	}


	public String getErrorLog() {
		return errorLog;
	}


	public void setErrorLog(String errorLog) {
		this.errorLog = errorLog;
	}


	public String getTicket() {
		return ticket;
	}


	public void setTicket(String ticket) {
		this.ticket = ticket;
	}


	public String getSessionId() {
		return sessionId;
	}


	public void setSessionId(String sessionId) {
		this.sessionId = sessionId;
	}


	public String getXmlRequest() {
		return xmlRequest;
	}


	public void setXmlRequest(String xmlRequest) {
		this.xmlRequest = xmlRequest;
	}


	public String getXmlResponse() {
		return xmlResponse;
	}


	public void setXmlResponse(String xmlResponse) {
		this.xmlResponse = xmlResponse;
	}


	public Integer getIdUsuario() {
		return idUsuario;
	}


	public void setIdUsuario(Integer idUsuario) {
		this.idUsuario = idUsuario;
	}


	public String getObjeto() {
		return objeto;
	}


	public void setObjeto(String objeto) {
		this.objeto = objeto;
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


	public String getSistemaOperativo() {
		return sistemaOperativo;
	}


	public void setSistemaOperativo(String sistemaOperativo) {
		this.sistemaOperativo = sistemaOperativo;
	}


	public String getDireccionip() {
		return direccionip;
	}


	public void setDireccionip(String direccionip) {
		this.direccionip = direccionip;
	}


	public String getParametrosLlamado() {
		return parametrosLlamado;
	}


	public void setParametrosLlamado(String parametrosLlamado) {
		this.parametrosLlamado = parametrosLlamado;
	}


	public boolean isActivo() {
		return activo;
	}


	public void setActivo(boolean activo) {
		this.activo = activo;
	}


	@Override
	public String toString() {
		return "LogErroresEntity [idLogError=" + idLogError + ", fechaCreacion=" + fechaCreacion + ", errorLog="
				+ errorLog + ", ticket=" + ticket + ", sessionId=" + sessionId + ", xmlRequest=" + xmlRequest
				+ ", xmlResponse=" + xmlResponse + ", idUsuario=" + idUsuario + ", objeto=" + objeto + ", longitud="
				+ longitud + ", latitud=" + latitud + ", pagina=" + pagina + ", explorador=" + explorador
				+ ", sistemaOperativo=" + sistemaOperativo + ", direccionip=" + direccionip + ", parametrosLlamado="
				+ parametrosLlamado + ", activo=" + activo + "]";
	}


	
	

}

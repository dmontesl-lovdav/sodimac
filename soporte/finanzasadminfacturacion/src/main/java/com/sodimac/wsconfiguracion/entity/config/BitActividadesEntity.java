package com.sodimac.wsconfiguracion.entity.config;

import java.io.Serializable;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Table;
import javax.persistence.Temporal;
import javax.persistence.TemporalType;

import org.hibernate.annotations.CreationTimestamp;

@Entity
@Table(name = "bitactividades")
public class BitActividadesEntity implements Serializable {

	/**
	 * 
	 */
	private static final long serialVersionUID = 8587982745409102036L;

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	@Column(name = "idBitActividad")
	private int idBitActividad;
	
	@Column(name = "idActividad")
	private int idActividad;

	@Column(name = "descripcion")
	private String descripcion;
	
	@Column(name = "ticket")
	private String ticket;
	
	@Column(name = "sessionId")
	private String sessionId;
	
	@Column(name = "fechaActividad")
	@CreationTimestamp
	@Temporal(TemporalType.TIMESTAMP)
	private java.util.Date fechaActividad;
	

	@Column(name = "longitud")
	private String longitud;
	
	@Column(name = "latitud")
	private String latitud;
	
	@Column(name = "pagina")
	private String pagina ;
	
	@Column(name = "explorador")
	private String explorador ;
	
	@Column(name = "sistemaOperativo")
	private String sistemaOperativo;
	
	@Column(name = "direccionIp")
	private String direccionIp;
	
	
	public int getIdBitActividad() {
		return idBitActividad;
	}



	public void setIdBitActividad(int idBitActividad) {
		this.idBitActividad = idBitActividad;
	}


	public int getIdActividad() {
		return idActividad;
	}


	public void setIdActividad(int idActividad) {
		this.idActividad = idActividad;
	}


	public String getDescripcion() {
		return descripcion;
	}



	public void setDescripcion(String descripcion) {
		this.descripcion = descripcion;
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


	public java.util.Date getFechaActividad() {
		return fechaActividad;
	}



	public void setFechaActividad(java.util.Date fechaActividad) {
		this.fechaActividad = fechaActividad;
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



	public String getDireccionIp() {
		return direccionIp;
	}



	public void setDireccionIp(String direccionIp) {
		this.direccionIp = direccionIp;
	}



	@Override
	public String toString() {
		return "BitActividadesEntity [idBitActividad=" + idBitActividad + ", idActividad=" + idActividad
				+ ", descripcion=" + descripcion + ", ticket=" + ticket + ", sessionId=" + sessionId
				+ ", fechaActividad=" + fechaActividad + ", longitud=" + longitud + ", latitud=" + latitud + ", pagina="
				+ pagina + ", explorador=" + explorador + ", sistemaOperativo=" + sistemaOperativo + ", direccionIp="
				+ direccionIp + "]";
	}
	
	
	
}

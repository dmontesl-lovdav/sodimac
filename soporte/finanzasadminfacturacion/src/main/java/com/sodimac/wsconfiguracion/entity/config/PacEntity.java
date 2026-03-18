package com.sodimac.wsconfiguracion.entity.config;

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
@Table(name = "pacs")
public class PacEntity {
	
	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	@Column(name = "idPac")
	private Integer idPac;
	
	@Column(name = "idPacExternal")
	private Integer idPacExternal;
	
	@Column(name = "nombrePac")
	private String nombrePac;
	
	@Column(name = "razonSocial")
	private String razonSocial;
	
	@Column(name = "rfc")
	private String rfc;
	
	@Column(name = "email")
	private String email;
	
	@Column(name = "endPoint")
	private String endPoint;
	
	@Column(name = "prioridad")
	private Integer prioridad;
	
	@Column(name = "reintentos")
	private Integer reintentos;
	
	@Column(name = "licencia")
	private String licencia;
	
	@Column(name = "usuario")
	private String usuario;
	
	@Column(name = "contrasena")
	private String contrasena;
	
	@Column(name = "activo")
	private Boolean activo;
	
	@Column(name = "fechaCreacion")
	@CreationTimestamp
	@Temporal(TemporalType.TIMESTAMP)
	private java.util.Date fechaCreacion;
	
	@Column(name = "fechaModificacion")
	@CreationTimestamp
	@Temporal(TemporalType.TIMESTAMP)
	private java.util.Date fechaModificacion;
	
	@Column(name = "fechaVigencia")
	@CreationTimestamp
	@Temporal(TemporalType.TIMESTAMP)
	private java.util.Date fechaVigencia;

	
	
	public Integer getIdPac() {
		return idPac;
	}

	public void setIdPac(Integer idPac) {
		this.idPac = idPac;
	}

	public Integer getIdPacExternal() {
		return idPacExternal;
	}

	public void setIdPacExternal(Integer idPacExternal) {
		this.idPacExternal = idPacExternal;
	}

	public String getNombrePac() {
		return nombrePac;
	}

	public void setNombrePac(String nombrePac) {
		this.nombrePac = nombrePac;
	}

	public String getRazonSocial() {
		return razonSocial;
	}

	public void setRazonSocial(String razonSocial) {
		this.razonSocial = razonSocial;
	}

	public String getRfc() {
		return rfc;
	}

	public void setRfc(String rfc) {
		this.rfc = rfc;
	}

	public String getEmail() {
		return email;
	}

	public void setEmail(String email) {
		this.email = email;
	}

	public String getEndPoint() {
		return endPoint;
	}

	public void setEndPoint(String endPoint) {
		this.endPoint = endPoint;
	}

	public Integer getPrioridad() {
		return prioridad;
	}

	public void setPrioridad(Integer prioridad) {
		this.prioridad = prioridad;
	}

	public Integer getReintentos() {
		return reintentos;
	}

	public void setReintentos(Integer reintentos) {
		this.reintentos = reintentos;
	}

	public String getLicencia() {
		return licencia;
	}

	public void setLicencia(String licencia) {
		this.licencia = licencia;
	}

	public String getUsuario() {
		return usuario;
	}

	public void setUsuario(String usuario) {
		this.usuario = usuario;
	}

	public String getContrasena() {
		return contrasena;
	}

	public void setContrasena(String contrasena) {
		this.contrasena = contrasena;
	}

	public Boolean getActivo() {
		return activo;
	}

	public void setActivo(Boolean activo) {
		this.activo = activo;
	}

	public java.util.Date getFechaCreacion() {
		return fechaCreacion;
	}

	public void setFechaCreacion(java.util.Date fechaCreacion) {
		this.fechaCreacion = fechaCreacion;
	}

	public java.util.Date getFechaModificacion() {
		return fechaModificacion;
	}

	public void setFechaModificacion(java.util.Date fechaModificacion) {
		this.fechaModificacion = fechaModificacion;
	}

	public java.util.Date getFechaVigencia() {
		return fechaVigencia;
	}

	public void setFechaVigencia(java.util.Date fechaVigencia) {
		this.fechaVigencia = fechaVigencia;
	}

	@Override
	public String toString() {
		return "PacEntity [idPac=" + idPac + ", idPacExternal=" + idPacExternal + ", nombrePac=" + nombrePac
				+ ", razonSocial=" + razonSocial + ", rfc=" + rfc + ", email=" + email + ", endPoint=" + endPoint
				+ ", prioridad=" + prioridad + ", reintentos=" + reintentos + ", licencia=" + licencia + ", usuario="
				+ usuario + ", contrasena=" + contrasena + ", activo=" + activo + ", fechaCreacion=" + fechaCreacion
				+ ", fechaModificacion=" + fechaModificacion + ", fechaVigencia=" + fechaVigencia + "]";
	}
	
	

}

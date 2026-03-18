package com.sodimac.facturacion.entity.fac.catalogospdf;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Table;
import javax.persistence.Temporal;
import javax.persistence.TemporalType;

import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.Type;

@Entity
@Table(name = "pacs")
public class PacsEntity {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	@Column(name = "idPac")
	private int idPac;
	
	@Column(name = "idPacExternal")
	private int idPacExternal;
	
	@Column(name = "nombrePac")
	private String nombrePac;
	
	@Column(name = "razonSocial")
	private String razonSocial;
	
	@Column(name = "rfc")
	private String rfc;
	
	@Column(name = "eMail")
	private String eMail;
	
	@Column(name = "endPoint")
	private String endPoint;
	
	@Column(name = "prioridad")
	private int prioridad;
	
	@Column(name = "reintentos")
	private int reintentos;
	
	@Column(name = "licencia")
	private String licencia;
	
	@Column(name = "usuario")
	private String usuario;
	
	@Column(name = "contrasena")
	private String contrasena;

	@Column(name = "activo", nullable = false, columnDefinition = "BIT", length = 1)
	@Type(type = "org.hibernate.type.NumericBooleanType")
	private boolean activo = true;
	
	@Column(name = "fechaCreacion")
	@CreationTimestamp
	@Temporal(TemporalType.TIMESTAMP)
	private java.util.Date fechaCreacion;
	
	@Column(name = "fechaVigencia")
	private java.util.Date fechaVigencia;

	@Column(name = "endPoint40")
	private String endPoint40;
	
	
	public PacsEntity() {
		super();
	}

	public int getIdPac() {
		return idPac;
	}

	public void setIdPac(int idPac) {
		this.idPac = idPac;
	}

	public int getIdPacExternal() {
		return idPacExternal;
	}

	public void setIdPacExternal(int idPacExternal) {
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

	public String geteMail() {
		return eMail;
	}

	public void seteMail(String eMail) {
		this.eMail = eMail;
	}

	public String getEndPoint() {
		return endPoint;
	}

	public void setEndPoint(String endPoint) {
		this.endPoint = endPoint;
	}

	public int getPrioridad() {
		return prioridad;
	}

	public void setPrioridad(int prioridad) {
		this.prioridad = prioridad;
	}

	public int getReintentos() {
		return reintentos;
	}

	public void setReintentos(int reintentos) {
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

	public boolean isActivo() {
		return activo;
	}

	public void setActivo(boolean activo) {
		this.activo = activo;
	}

	public java.util.Date getFechaCreacion() {
		return fechaCreacion;
	}

	public void setFechaCreacion(java.util.Date fechaCreacion) {
		this.fechaCreacion = fechaCreacion;
	}

	public java.util.Date getFechaVigencia() {
		return fechaVigencia;
	}

	public void setFechaVigencia(java.util.Date fechaVigencia) {
		this.fechaVigencia = fechaVigencia;
	}

	public String getEndPoint40() {
		return endPoint40;
	}

	public void setEndPoint40(String endPoint40) {
		this.endPoint40 = endPoint40;
	}

	
}

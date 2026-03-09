package com.sodimac.cfdi.entity.fiscal;

import java.util.Date;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;
import javax.persistence.Temporal;
import javax.persistence.TemporalType;

import org.hibernate.annotations.CreationTimestamp;

@Entity
@Table(name = "configuracionftp")
public class ConfiguracionFtpEntity {

	@Id
	@Column(name = "idConfiguracionFtp")
	private Integer idConfiguracionFtp;

	@Column(name = "usuario")
	private String usuario;

	@Column(name = "descripcion")
	private String descripcion;

	@Column(name = "contrasenia")
	private String contrasenia;

	@Column(name = "puerto")
	private Integer puerto;

	@Column(name = "url")
	private String url;

	@Column(name = "estatus")
	private Integer estatus;

	@Column(name = "fechaCreacion")
	@CreationTimestamp
	@Temporal(TemporalType.TIMESTAMP)
	private Date fechaCreacion;

	public Integer getIdConfiguracionFtp() {
		return idConfiguracionFtp;
	}

	public void setIdConfiguracionFtp(Integer idConfiguracionFtp) {
		this.idConfiguracionFtp = idConfiguracionFtp;
	}

	public String getUsuario() {
		return usuario;
	}

	public void setUsuario(String usuario) {
		this.usuario = usuario;
	}

	public String getDescripcion() {
		return descripcion;
	}

	public void setDescripcion(String descripcion) {
		this.descripcion = descripcion;
	}

	public String getContrasenia() {
		return contrasenia;
	}

	public void setContrasenia(String contrasenia) {
		this.contrasenia = contrasenia;
	}

	public Integer getPuerto() {
		return puerto;
	}

	public void setPuerto(Integer puerto) {
		this.puerto = puerto;
	}

	public String getUrl() {
		return url;
	}

	public void setUrl(String url) {
		this.url = url;
	}

	public Integer getEstatus() {
		return estatus;
	}

	public void setEstatus(Integer estatus) {
		this.estatus = estatus;
	}

	public Date getFechaCreacion() {
		return fechaCreacion;
	}

	public void setFechaCreacion(Date fechaCreacion) {
		this.fechaCreacion = fechaCreacion;
	}

}

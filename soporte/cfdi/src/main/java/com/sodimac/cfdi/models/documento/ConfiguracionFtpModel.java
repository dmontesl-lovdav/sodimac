package com.sodimac.cfdi.models.documento;

import java.util.Date;

public class ConfiguracionFtpModel {

	private Integer idConfiguracionFtp;
	private String usuario;
	private String descripcion;
	private String contrasenia;
	private Integer puerto;
	private String url;
	private Integer estatus;
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

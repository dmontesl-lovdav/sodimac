package com.sodimac.cfdi.entity.fiscal;

import java.util.Date;

import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;
import javax.persistence.Temporal;
import javax.persistence.TemporalType;

import org.hibernate.annotations.CreationTimestamp;

@Entity
@Table(name = "procesodescarga")
public class ProcesoDescargaEntity {

	@Id
	private String idEjecucion;
	@CreationTimestamp
	@Temporal(TemporalType.TIMESTAMP)
	private Date fechaSolicitud;
	@CreationTimestamp
	@Temporal(TemporalType.TIMESTAMP)
	private Date fechaGeneracion;
	private String parametros;
	private String modulo;
	private String usuario;
	private Integer estatus;
	private String mensaje;
	private String listaArchivos;
	
	public String getIdEjecucion() {
		return idEjecucion;
	}
	public void setIdEjecucion(String idEjecucion) {
		this.idEjecucion = idEjecucion;
	}
	public Date getFechaSolicitud() {
		return fechaSolicitud;
	}
	public void setFechaSolicitud(Date fechaSolicitud) {
		this.fechaSolicitud = fechaSolicitud;
	}
	public Date getFechaGeneracion() {
		return fechaGeneracion;
	}
	public void setFechaGeneracion(Date fechaGeneracion) {
		this.fechaGeneracion = fechaGeneracion;
	}
	public String getParametros() {
		return parametros;
	}
	public void setParametros(String parametros) {
		this.parametros = parametros;
	}
	public String getModulo() {
		return modulo;
	}
	public void setModulo(String modulo) {
		this.modulo = modulo;
	}
	public String getUsuario() {
		return usuario;
	}
	public void setUsuario(String usuario) {
		this.usuario = usuario;
	}
	public Integer getEstatus() {
		return estatus;
	}
	public void setEstatus(Integer estatus) {
		this.estatus = estatus;
	}
	public String getMensaje() {
		return mensaje;
	}
	public void setMensaje(String mensaje) {
		this.mensaje = mensaje;
	}
	public String getListaArchivos() {
		return listaArchivos;
	}
	public void setListaArchivos(String listaArchivos) {
		this.listaArchivos = listaArchivos;
	}

}

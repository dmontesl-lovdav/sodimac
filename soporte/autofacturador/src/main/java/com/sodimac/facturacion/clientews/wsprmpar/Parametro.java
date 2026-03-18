package com.sodimac.facturacion.clientews.wsprmpar;

import java.util.Date;

public class Parametro {

	String nombreCampo;
	String valor;
	String descripcion;
	String aplicacion;
	int idGrupoUsuario;
	int idTipoDato;
	Date fechaCreacion;
	boolean activo;
	String valorInactivo;
	
	public String getNombreCampo() {
		return nombreCampo;
	}
	public void setNombreCampo(String nombreCampo) {
		this.nombreCampo = nombreCampo;
	}
	public String getValor() {
		return valor;
	}
	public void setValor(String valor) {
		this.valor = valor;
	}
	public String getDescripcion() {
		return descripcion;
	}
	public void setDescripcion(String descripcion) {
		this.descripcion = descripcion;
	}
	public String getAplicacion() {
		return aplicacion;
	}
	public void setAplicacion(String aplicacion) {
		this.aplicacion = aplicacion;
	}
	public int getIdGrupoUsuario() {
		return idGrupoUsuario;
	}
	public void setIdGrupoUsuario(int idGrupoUsuario) {
		this.idGrupoUsuario = idGrupoUsuario;
	}
	public int getIdTipoDato() {
		return idTipoDato;
	}
	public void setIdTipoDato(int idTipoDato) {
		this.idTipoDato = idTipoDato;
	}
	public Date getFechaCreacion() {
		return fechaCreacion;
	}
	public void setFechaCreacion(Date fechaCreacion) {
		this.fechaCreacion = fechaCreacion;
	}
	public boolean isActivo() {
		return activo;
	}
	public void setActivo(boolean activo) {
		this.activo = activo;
	}
	public String getValorInactivo() {
		return valorInactivo;
	}
	public void setValorInactivo(String valorInactivo) {
		this.valorInactivo = valorInactivo;
	}
	
}

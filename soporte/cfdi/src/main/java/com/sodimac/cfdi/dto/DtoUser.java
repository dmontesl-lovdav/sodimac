/**
 * 
 */
package com.sodimac.cfdi.dto;

import com.fasterxml.jackson.annotation.JsonProperty;

public class DtoUser {

	@JsonProperty("IdUsuario")
	private int idUsuario;
	@JsonProperty("Usuario")
	private String usuario;
	@JsonProperty("Password")
	private String password;
	@JsonProperty("Nombre")
	private String nombre;
	@JsonProperty("SegundoNombre")
	private Object segundoNombre;
	@JsonProperty("ApellidoP")
	private String apellidoP;
	@JsonProperty("ApellidoM")
	private String apellidoM;
	@JsonProperty("Activo")
	private boolean activo;
	@JsonProperty("CambiarPass")
	private boolean cambiarPass;
	@JsonProperty("IdPerfil")
	private int idPerfil;
	@JsonProperty("PerfilDescripcion")
	private String perfilDescripcion;
	@JsonProperty("FechaAlta")
	private Object fechaAlta;
	@JsonProperty("OldPassword")
	private String oldPassword;
	private String navigator;

	public int getIdUsuario() {
		return idUsuario;
	}

	public void setIdUsuario(int idUsuario) {
		this.idUsuario = idUsuario;
	}

	public String getUsuario() {
		return usuario;
	}

	public void setUsuario(String usuario) {
		this.usuario = usuario;
	}

	public String getPassword() {
		return password;
	}

	public void setPassword(String password) {
		this.password = password;
	}

	public String getNombre() {
		return nombre;
	}

	public void setNombre(String nombre) {
		this.nombre = nombre;
	}

	public Object getSegundoNombre() {
		return segundoNombre;
	}

	public void setSegundoNombre(Object segundoNombre) {
		this.segundoNombre = segundoNombre;
	}

	public String getApellidoP() {
		return apellidoP;
	}

	public void setApellidoP(String apellidoP) {
		this.apellidoP = apellidoP;
	}

	public String getApellidoM() {
		return apellidoM;
	}

	public void setApellidoM(String apellidoM) {
		this.apellidoM = apellidoM;
	}

	public boolean isActivo() {
		return activo;
	}

	public void setActivo(boolean activo) {
		this.activo = activo;
	}

	public boolean isCambiarPass() {
		return cambiarPass;
	}

	public void setCambiarPass(boolean cambiarPass) {
		this.cambiarPass = cambiarPass;
	}

	public int getIdPerfil() {
		return idPerfil;
	}

	public void setIdPerfil(int idPerfil) {
		this.idPerfil = idPerfil;
	}

	public String getPerfilDescripcion() {
		return perfilDescripcion;
	}

	public void setPerfilDescripcion(String perfilDescripcion) {
		this.perfilDescripcion = perfilDescripcion;
	}

	public Object getFechaAlta() {
		return fechaAlta;
	}

	public void setFechaAlta(Object fechaAlta) {
		this.fechaAlta = fechaAlta;
	}

	public String getOldPassword() {
		return oldPassword;
	}

	public void setOldPassword(String oldPassword) {
		this.oldPassword = oldPassword;
	}

	public String getNavigator() {
		return navigator;
	}

	public void setNavigator(String navigator) {
		this.navigator = navigator;
	}

}

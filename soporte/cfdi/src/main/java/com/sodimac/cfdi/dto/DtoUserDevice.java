/**
 * 
 */
package com.sodimac.cfdi.dto;

import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * @author jfalvarez
 *
 */
public class DtoUserDevice {

	@JsonProperty("IdUsuario")
	public int idUsuario;
	@JsonProperty("Usuario")
	public String usuario;
	@JsonProperty("Password")
	public String password;
	@JsonProperty("Nombre")
	public String nombre;
	@JsonProperty("SegundoNombre")
	public String segundoNombre;
	@JsonProperty("ApellidoP")
	public String apellidoP;
	@JsonProperty("ApellidoM")
	public String apellidoM;
	@JsonProperty("FechaAlta")
	public String fechaAlta;
	@JsonProperty("CambiarPass")
	public boolean cambiarPass;
	@JsonProperty("Activo")
	public boolean activo;

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

	public String getSegundoNombre() {
		return segundoNombre;
	}

	public void setSegundoNombre(String segundoNombre) {
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

	public String getFechaAlta() {
		return fechaAlta;
	}

	public void setFechaAlta(String fechaAlta) {
		this.fechaAlta = fechaAlta;
	}

	public boolean isCambiarPass() {
		return cambiarPass;
	}

	public void setCambiarPass(boolean cambiarPass) {
		this.cambiarPass = cambiarPass;
	}

	public boolean isActivo() {
		return activo;
	}

	public void setActivo(boolean activo) {
		this.activo = activo;
	}

}

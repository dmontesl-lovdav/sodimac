/**
 * 
 */
package com.sodimac.cfdi.dto;

import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * @author jfalvarez
 *
 */
public class DtoConfiguration {

	@JsonProperty("IdConfiguracion")
	public int idConfiguracion;
	@JsonProperty("Nombre")
	public String nombre;
	@JsonProperty("Valor")
	public String valor;
	@JsonProperty("Descripcion")
	public String descripcion;
	@JsonProperty("Activo")
	public String activo;

	public int getIdConfiguracion() {
		return idConfiguracion;
	}

	public void setIdConfiguracion(int idConfiguracion) {
		this.idConfiguracion = idConfiguracion;
	}

	public String getNombre() {
		return nombre;
	}

	public void setNombre(String nombre) {
		this.nombre = nombre;
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

	public String getActivo() {
		return activo;
	}

	public void setActivo(String activo) {
		this.activo = activo;
	}

}

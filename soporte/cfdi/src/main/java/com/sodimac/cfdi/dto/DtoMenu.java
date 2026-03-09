/**
 * 
 */
package com.sodimac.cfdi.dto;

import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * @author jfalvarez
 *
 */
public class DtoMenu {

	@JsonProperty("IdOpcion")
	public int idOpcion;
	@JsonProperty("CodOpcion")
	public String codOpcion;
	@JsonProperty("Descripcion")
	public String descripcion;
	@JsonProperty("Link")
	public String link;
	@JsonProperty("PadreId")
	public int padreId;
	@JsonProperty("Estatus")
	public int estatus;
	@JsonProperty("FechaCreacion")
	public String fechaCreacion;

	public int getIdOpcion() {
		return idOpcion;
	}

	public void setIdOpcion(int idOpcion) {
		this.idOpcion = idOpcion;
	}

	public String getCodOpcion() {
		return codOpcion;
	}

	public void setCodOpcion(String codOpcion) {
		this.codOpcion = codOpcion;
	}

	public String getDescripcion() {
		return descripcion;
	}

	public void setDescripcion(String descripcion) {
		this.descripcion = descripcion;
	}

	public String getLink() {
		return link;
	}

	public void setLink(String link) {
		this.link = link;
	}

	public int getPadreId() {
		return padreId;
	}

	public void setPadreId(int padreId) {
		this.padreId = padreId;
	}

	public int getEstatus() {
		return estatus;
	}

	public void setEstatus(int estatus) {
		this.estatus = estatus;
	}

	public String getFechaCreacion() {
		return fechaCreacion;
	}

	public void setFechaCreacion(String fechaCreacion) {
		this.fechaCreacion = fechaCreacion;
	}

}

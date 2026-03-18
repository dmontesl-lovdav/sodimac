package com.sodimac.wsconfiguracion.dto;

public class MesDto {

	private Integer idMes;
	private String clave;
	private String descripcion;
	private boolean activo = true;
	public Integer getIdMes() {
		return idMes;
	}
	public void setIdMes(Integer idMes) {
		this.idMes = idMes;
	}
	public String getClave() {
		return clave;
	}
	public void setClave(String clave) {
		this.clave = clave;
	}
	public String getDescripcion() {
		return descripcion;
	}
	public void setDescripcion(String descripcion) {
		this.descripcion = descripcion;
	}
	public boolean isActivo() {
		return activo;
	}
	public void setActivo(boolean activo) {
		this.activo = activo;
	}
	
	@Override
	public String toString() {
		return "MesesDto [idMes=" + idMes + ", clave=" + clave + ", descripcion=" + descripcion + ", activo=" + activo
				+ "]";
	}
}

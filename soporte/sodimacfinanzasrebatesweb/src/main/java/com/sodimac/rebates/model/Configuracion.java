package com.sodimac.rebates.model;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Table;

@Entity
@Table(name = "CatConfiguracion")
public class Configuracion {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Integer idCatConfiguracion;
	private String valor;
	private String nombreVariable;
	private boolean activo;

	public Integer getIdCatConfiguracion() {
		return idCatConfiguracion;
	}

	public void setIdCatConfiguracion(Integer idCatConfiguracion) {
		this.idCatConfiguracion = idCatConfiguracion;
	}

	public String getValor() {
		return valor;
	}

	public void setValor(String valor) {
		this.valor = valor;
	}

	public String getNombreVariable() {
		return nombreVariable;
	}

	public void setNombreVariable(String nombreVariable) {
		this.nombreVariable = nombreVariable;
	}

	public boolean isActivo() {
		return activo;
	}

	public void setActivo(boolean activo) {
		this.activo = activo;
	}

	@Override
	public String toString() {
		return "Configuracion [idCatConfiguracion=" + idCatConfiguracion + ", valor=" + valor + ", nombreVariable="
				+ nombreVariable + ", activo=" + activo + "]";
	}

}

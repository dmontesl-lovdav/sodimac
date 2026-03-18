package com.sodimac.facturacion.entity.fac;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;

import org.hibernate.annotations.Type;

@Entity
@Table(name = "confToken")
public class ConfiguracionTokenEntity {

	@Id
	@Column(name = "idConfToken")
	private int idConfToken;

	@Column(name = "longitud")
	private int longitud;

	@Column(name = "mayusculas", nullable = false, columnDefinition = "BIT", length = 1)
	@Type(type = "org.hibernate.type.NumericBooleanType")
	private boolean mayusculas = true;

	@Column(name = "minusculas", nullable = false, columnDefinition = "BIT", length = 1)
	@Type(type = "org.hibernate.type.NumericBooleanType")
	private boolean minusculas = true;

	@Column(name = "numeros", nullable = false, columnDefinition = "BIT", length = 1)
	@Type(type = "org.hibernate.type.NumericBooleanType")
	private boolean numeros = true;

	@Column(name = "tiempoVida")
	private int tiempoVida;

	@Column(name = "reenvios")
	private int reenvios;	

	public ConfiguracionTokenEntity() {

	}

	public int getIdConfToken() {
		return idConfToken;
	}

	public void setIdConfToken(int idConfToken) {
		this.idConfToken = idConfToken;
	}

	public int getLongitud() {
		return longitud;
	}

	public void setLongitud(int longitud) {
		this.longitud = longitud;
	}

	public boolean isMayusculas() {
		return mayusculas;
	}

	public void setMayusculas(boolean mayusculas) {
		this.mayusculas = mayusculas;
	}

	public boolean isMinusculas() {
		return minusculas;
	}

	public void setMinusculas(boolean minusculas) {
		this.minusculas = minusculas;
	}

	public boolean isNumeros() {
		return numeros;
	}

	public void setNumeros(boolean numeros) {
		this.numeros = numeros;
	}

	public int getTiempoVida() {
		return tiempoVida;
	}

	public void setTiempoVida(int tiempoVida) {
		this.tiempoVida = tiempoVida;
	}

	public int getReenvios() {
		return reenvios;
	}

	public void setReenvios(int reenvios) {
		this.reenvios = reenvios;
	}

	@Override
	public String toString() {
		return "ConfiguracionTokenEntity [idConfToken=" + idConfToken + ", longitud=" + longitud + ", mayusculas="
				+ mayusculas + ", minusculas=" + minusculas + ", numeros=" + numeros + ", tiempoVida=" + tiempoVida
				+ ", reenvios=" + reenvios + "]";
	}
	
}

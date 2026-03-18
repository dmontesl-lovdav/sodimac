package com.sodimac.wsconfiguracion.entity.config;

import java.time.LocalDate;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.PostLoad;
import javax.persistence.Table;
import javax.persistence.Transient;

@Entity
@Table(name = "catcodigopostal")
public class CatCodigoPostalEntity {
	
	
	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private int id;
	
	@Transient
	private int codigopostal;
	
	@Column(name = "c_estado")
	private String c_estado;
	
	@Column(name = "c_municipio")
	private String c_municipio;
	
	@Column(name = "c_localidad")
	private String c_localidad;
	
	@Column(name = "estimulofranjafronteriza")
	private int estimulofranjafronteriza;
	
	@Column(name = "fechainiciovigencia")
	private LocalDate fechainiciovigencia;
	
	@Column(name = "fehafinvigencia")
	private LocalDate fehafinvigencia;

	public int getId() {
		return id;
	}

	public void setId(int id) {
		this.id = id;
	}



	public int getCodigopostal() {
		return codigopostal;
	}

	public void setCodigopostal(int codigopostal) {
		this.codigopostal = codigopostal;
	}

	public String getC_estado() {
		return c_estado;
	}

	public void setC_estado(String c_estado) {
		this.c_estado = c_estado;
	}

	public String getC_municipio() {
		return c_municipio;
	}

	public void setC_municipio(String c_municipio) {
		this.c_municipio = c_municipio;
	}

	public String getC_localidad() {
		return c_localidad;
	}

	public void setC_localidad(String c_localidad) {
		this.c_localidad = c_localidad;
	}

	public int getEstimulofranjafronteriza() {
		return estimulofranjafronteriza;
	}

	public void setEstimulofranjafronteriza(int estimulofranjafronteriza) {
		this.estimulofranjafronteriza = estimulofranjafronteriza;
	}

	public LocalDate getFechainiciovigencia() {
		return fechainiciovigencia;
	}

	public void setFechainiciovigencia(LocalDate fechainiciovigencia) {
		this.fechainiciovigencia = fechainiciovigencia;
	}

	public LocalDate getFehafinvigencia() {
		return fehafinvigencia;
	}

	public void setFehafinvigencia(LocalDate fehafinvigencia) {
		this.fehafinvigencia = fehafinvigencia;
	}

	@PostLoad
	public void seteaCodigoPostal() {
		this.codigopostal = this.id;
	}
	
	@Override
	public String toString() {
		return "CatCodigoPostalEntity [id=" + id + ", codigopostal=" + codigopostal + ", c_estado=" + c_estado
				+ ", c_municipio=" + c_municipio + ", c_localidad=" + c_localidad + ", estimulofranjafronteriza="
				+ estimulofranjafronteriza + ", fechainiciovigencia=" + fechainiciovigencia + ", fehafinvigencia="
				+ fehafinvigencia + "]";
	}


	

}

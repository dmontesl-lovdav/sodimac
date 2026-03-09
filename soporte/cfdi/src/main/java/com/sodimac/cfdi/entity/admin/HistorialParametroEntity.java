package com.sodimac.cfdi.entity.admin;

import java.util.Date;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Table;

@Entity
@Table(name = "hist_parametros")
public class HistorialParametroEntity {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	int regid;
	String usuario;
	String parametro;
	String accion;
	Date fecha;

	public String getUsuario() {
		return usuario;
	}

	public void setUsuario(String usuario) {
		this.usuario = usuario;
	}

	public String getParametro() {
		return parametro;
	}

	public void setParametro(String parametro) {
		this.parametro = parametro;
	}

	public String getAccion() {
		return accion;
	}

	public void setAccion(String accion) {
		this.accion = accion;
	}

	public Date getFecha() {
		return fecha;
	}

	public void setFecha(Date fecha) {
		this.fecha = fecha;
	}

}

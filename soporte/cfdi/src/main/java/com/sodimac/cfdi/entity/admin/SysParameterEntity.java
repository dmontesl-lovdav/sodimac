package com.sodimac.cfdi.entity.admin;

import java.util.Date;

import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;

@Entity
@Table(name = "catconfiguracion")
public class SysParameterEntity {

	@Id
	String nombreCampo;
	String valor;
	String aplicacion;
	String descripcion;
	Date fechaCreacion;
	String idGrupoUsuario;
	String idTipoDato;
	int activo;
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

	public String getAplicacion() {
		return aplicacion;
	}

	public void setAplicacion(String aplicacion) {
		this.aplicacion = aplicacion;
	}

	public String getDescripcion() {
		return descripcion;
	}

	public void setDescripcion(String descripcion) {
		this.descripcion = descripcion;
	}

	public Date getFechaCreacion() {
		return fechaCreacion;
	}

	public void setFechaCreacion(Date fechaCreacion) {
		this.fechaCreacion = fechaCreacion;
	}

	public String getIdGrupoUsuario() {
		return idGrupoUsuario;
	}

	public void setIdGrupoUsuario(String idGrupoUsuario) {
		this.idGrupoUsuario = idGrupoUsuario;
	}

	public String getIdTipoDato() {
		return idTipoDato;
	}

	public void setIdTipoDato(String idTipoDato) {
		this.idTipoDato = idTipoDato;
	}

	public int getActivo() {
		return activo;
	}

	public void setActivo(int activo) {
		this.activo = activo;
	}

	public String getValorInactivo() {
		return valorInactivo;
	}

	public void setValorInactivo(String valorInactivo) {
		this.valorInactivo = valorInactivo;
	}

}
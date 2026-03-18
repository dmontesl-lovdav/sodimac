package com.sodimac.wsconfiguracion.entity.config;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Lob;
import javax.persistence.Table;
import javax.persistence.Temporal;
import javax.persistence.TemporalType;

import org.hibernate.annotations.CreationTimestamp;

@Entity
@Table(name = "catconfiguracion")
public class CatConfiguracionEntity {

	@Id
	@Column(name = "id")
	private Integer id;
	
	@Column(name = "nombreCampo")
	private String nombreCampo;

	@Lob
	@Column(name = "valor")
	private String valor;

	@Column(name = "aplicacion")
	private String aplicacion;

	@Column(name = "descripcion")
	private String descripcion;
	
	@Column(name = "idGrupoUsuario")
	private Integer idGrupoUsuario;
	
	@Column(name = "idTipoDato")
	private Integer idTipoDato;
	
	@Column(name = "activo")
	private Boolean activo;

	@Column(name = "fechaCreacion")
	@CreationTimestamp
	@Temporal(TemporalType.TIMESTAMP)
	private java.util.Date fechaCreacion;
	
	@Column(name = "fechaModificacion")
	@CreationTimestamp
	@Temporal(TemporalType.TIMESTAMP)
	private java.util.Date fechaModificacion;

	public Integer getId() {
		return id;
	}

	public void setId(Integer id) {
		this.id = id;
	}

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

	public Integer getIdGrupoUsuario() {
		return idGrupoUsuario;
	}

	public void setIdGrupoUsuario(Integer idGrupoUsuario) {
		this.idGrupoUsuario = idGrupoUsuario;
	}

	public Integer getIdTipoDato() {
		return idTipoDato;
	}

	public void setIdTipoDato(Integer idTipoDato) {
		this.idTipoDato = idTipoDato;
	}

	public Boolean getActivo() {
		return activo;
	}

	public void setActivo(Boolean activo) {
		this.activo = activo;
	}

	public java.util.Date getFechaCreacion() {
		return fechaCreacion;
	}

	public void setFechaCreacion(java.util.Date fechaCreacion) {
		this.fechaCreacion = fechaCreacion;
	}

	public java.util.Date getFechaModificacion() {
		return fechaModificacion;
	}

	public void setFechaModificacion(java.util.Date fechaModificacion) {
		this.fechaModificacion = fechaModificacion;
	}

	@Override
	public String toString() {
		return "CatConfiguracionEntity [id=" + id + ", nombreCampo=" + nombreCampo + ", valor=" + valor
				+ ", aplicacion=" + aplicacion + ", descripcion=" + descripcion + ", idGrupoUsuario=" + idGrupoUsuario
				+ ", idTipoDato=" + idTipoDato + ", activo=" + activo + ", fechaCreacion=" + fechaCreacion
				+ ", fechaModificacion=" + fechaModificacion + "]";
	}
	
	
	
}

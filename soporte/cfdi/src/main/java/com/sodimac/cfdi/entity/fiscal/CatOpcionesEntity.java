package com.sodimac.cfdi.entity.fiscal;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Table;
import javax.persistence.Temporal;
import javax.persistence.TemporalType;

import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.Type;

@Entity
@Table(name = "catOpciones")
public class CatOpcionesEntity {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	@Column(name = "idOpcion")
	private Integer idOpcion;

	@Column(name = "codOpcion")
	private String codOpcion;

	@Column(name = "nomOpcion")
	private String nomOpcion;

	@Column(name = "link")
	private String link;
	
	@Column(name = "padre_id")
	private Integer idPadre;
	
	@Column(name = "activo", nullable = false, columnDefinition = "BIT", length = 1)
	@Type(type = "org.hibernate.type.NumericBooleanType")
	private boolean activo = true;
	
	@Column(name = "fechaCreacion")
	@CreationTimestamp
	@Temporal(TemporalType.TIMESTAMP)
	private java.util.Date fechaCreacion;

	public CatOpcionesEntity() {

	}

	public Integer getIdOpcion() {
		return idOpcion;
	}

	public void setIdOpcion(Integer idOpcion) {
		this.idOpcion = idOpcion;
	}

	public String getCodOpcion() {
		return codOpcion;
	}

	public void setCodOpcion(String codOpcion) {
		this.codOpcion = codOpcion;
	}

	public String getNomOpcion() {
		return nomOpcion;
	}

	public void setNomOpcion(String nomOpcion) {
		this.nomOpcion = nomOpcion;
	}

	public String getLink() {
		return link;
	}

	public void setLink(String link) {
		this.link = link;
	}

	public Integer getIdPadre() {
		return idPadre;
	}

	public void setIdPadre(Integer idPadre) {
		this.idPadre = idPadre;
	}

	public boolean isActivo() {
		return activo;
	}

	public void setActivo(boolean activo) {
		this.activo = activo;
	}

	public java.util.Date getFechaCreacion() {
		return fechaCreacion;
	}

	public void setFechaCreacion(java.util.Date fechaCreacion) {
		this.fechaCreacion = fechaCreacion;
	}

	@Override
	public String toString() {
		return "CatOpcionesEntity [idOpcion=" + idOpcion + ", codOpcion=" + codOpcion + ", nomOpcion=" + nomOpcion
				+ ", link=" + link + ", idPadre=" + idPadre + ", activo=" + activo + ", fechaCreacion=" + fechaCreacion
				+ "]";
	}

}

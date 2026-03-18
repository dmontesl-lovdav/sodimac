package com.sodimac.wsconfiguracion.entity.config;

import java.math.BigInteger;
import java.util.Date;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.PreUpdate;
import javax.persistence.Table;
import javax.persistence.Temporal;
import javax.persistence.TemporalType;

import org.hibernate.annotations.CreationTimestamp;

@Entity
@Table(name = "folio")
public class FolioEntity {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	@Column(name = "id")
	private BigInteger id;
	
	@Column(name = "idcatserie")
	private Integer idcatserie;
	
	@Column(name = "idconfdatosemisortienda")
	private Integer idconfdatosemisortienda;
	
	@Column(name = "folio")
	private BigInteger folio;
	
	@Column(name = "fechaCreacion")
	@CreationTimestamp
	@Temporal(TemporalType.TIMESTAMP)
	private java.util.Date fechaCreacion;
	
	@Column(name = "fechaModificacion")
	@Temporal(TemporalType.TIMESTAMP)
	private java.util.Date fechaModificacion;

	
	public FolioEntity() {}
	
	public FolioEntity(Integer idcatserie, Integer idconfdatosemisortienda) {
		this.idcatserie = idcatserie;
		this.idconfdatosemisortienda = idconfdatosemisortienda;
	}
	
	public BigInteger getId() {
		return id;
	}

	public void setId(BigInteger id) {
		this.id = id;
	}

	public Integer getIdcatserie() {
		return idcatserie;
	}

	public void setIdcatserie(Integer idcatserie) {
		this.idcatserie = idcatserie;
	}

	public Integer getIdconfdatosemisortienda() {
		return idconfdatosemisortienda;
	}

	public void setIdconfdatosemisortienda(Integer idconfdatosemisortienda) {
		this.idconfdatosemisortienda = idconfdatosemisortienda;
	}

	public BigInteger getFolio() {
		return folio;
	}

	public void setFolio(BigInteger folio) {
		this.folio = folio;
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
	
	@PreUpdate
	public void actualizaFechaModificacion() {
		this.fechaModificacion = new Date();
	}
}

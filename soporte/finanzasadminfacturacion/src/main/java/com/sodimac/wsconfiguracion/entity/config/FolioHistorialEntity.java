package com.sodimac.wsconfiguracion.entity.config;

import java.math.BigInteger;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Table;
import javax.persistence.Temporal;
import javax.persistence.TemporalType;

import org.hibernate.annotations.CreationTimestamp;

@Entity
@Table(name = "foliohistorial")
public class FolioHistorialEntity {

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
	
	@Column(name = "idcataplicaciones")
	private Integer idcataplicaciones;
	
	@Column(name = "serietiendastr")
	private String serietiendastr;
	
	@Column(name = "fechaCreacion")
	@CreationTimestamp
	@Temporal(TemporalType.TIMESTAMP)
	private java.util.Date fechaCreacion;
	
	@Column(name = "fechaCreaciondate")
	@CreationTimestamp
	@Temporal(TemporalType.TIMESTAMP)
	private java.util.Date fechaCreaciondate;

	
	public FolioHistorialEntity() {}
	
	public FolioHistorialEntity(Integer idcatserie, Integer idconfdatosemisortienda, Integer idcataplicaciones, String serietiendastr) {
		this.idcatserie = idcatserie;
		this.idconfdatosemisortienda = idconfdatosemisortienda;
		this.idcataplicaciones = idcataplicaciones;
		this.serietiendastr = serietiendastr;
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

	public Integer getIdcataplicaciones() {
		return idcataplicaciones;
	}

	public void setIdcataplicaciones(Integer idcataplicaciones) {
		this.idcataplicaciones = idcataplicaciones;
	}

	public String getSerietiendastr() {
		return serietiendastr;
	}

	public void setSerietiendastr(String serietiendastr) {
		this.serietiendastr = serietiendastr;
	}

	public java.util.Date getFechaCreacion() {
		return fechaCreacion;
	}

	public void setFechaCreacion(java.util.Date fechaCreacion) {
		this.fechaCreacion = fechaCreacion;
	}

	public java.util.Date getFechaCreaciondate() {
		return fechaCreaciondate;
	}

	public void setFechaCreaciondate(java.util.Date fechaCreaciondate) {
		this.fechaCreaciondate = fechaCreaciondate;
	}

	@Override
	public String toString() {
		return "HistorialFolioEntity [id=" + id + ", idcatserie=" + idcatserie + ", idconfdatosemisortienda="
				+ idconfdatosemisortienda + ", folio=" + folio + ", idcataplicaciones=" + idcataplicaciones
				+ ", serietiendastr=" + serietiendastr + ", fechaCreacion=" + fechaCreacion + ", fechaCreaciondate="
				+ fechaCreaciondate + "]";
	}
	
	
}

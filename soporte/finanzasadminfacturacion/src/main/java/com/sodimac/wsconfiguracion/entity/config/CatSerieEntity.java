package com.sodimac.wsconfiguracion.entity.config;

import java.util.Date;

import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.Table;
import javax.persistence.Temporal;
import javax.persistence.TemporalType;

import org.hibernate.annotations.CreationTimestamp;

@Entity
@Table(name = "catserie")
public class CatSerieEntity {
	
	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	@Column(name = "id")
	private Integer id;
	
	@Column(name = "serie")
	private String serie;
	
	@Column(name = "descripcion")
	private String descripcion;
	
    @ManyToOne(optional = false, cascade = CascadeType.ALL, fetch = FetchType.EAGER)
    @JoinColumn(name="idcattipocomprobantesodimac")
    private CatTipoComprobanteSodimacEntity catTipoComprobanteSodimacEntity;
    
    @ManyToOne(optional = false, cascade = CascadeType.ALL, fetch = FetchType.EAGER)
    @JoinColumn(name="idcattipotienda")
    private CatTipoTiendaEntity catTipoTiendaEntity;
	
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

	public String getSerie() {
		return serie;
	}

	public void setSerie(String serie) {
		this.serie = serie;
	}

	public String getDescripcion() {
		return descripcion;
	}

	public void setDescripcion(String descripcion) {
		this.descripcion = descripcion;
	}

	public CatTipoComprobanteSodimacEntity getCatTipoComprobanteSodimacEntity() {
		return catTipoComprobanteSodimacEntity;
	}

	public void setCatTipoComprobanteSodimacEntity(CatTipoComprobanteSodimacEntity catTipoComprobanteSodimacEntity) {
		this.catTipoComprobanteSodimacEntity = catTipoComprobanteSodimacEntity;
	}

	public CatTipoTiendaEntity getCatTipoTiendaEntity() {
		return catTipoTiendaEntity;
	}

	public void setCatTipoTiendaEntity(CatTipoTiendaEntity catTipoTiendaEntity) {
		this.catTipoTiendaEntity = catTipoTiendaEntity;
	}

	public Boolean getActivo() {
		return activo;
	}

	public void setActivo(Boolean activo) {
		this.activo = activo;
	}

	public Date getFechaCreacion() {
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
		return "CatSerieEntity [id=" + id + ", serie=" + serie + ", descripcion=" + descripcion
				+ ", catTipoComprobanteSodimacEntity=" + catTipoComprobanteSodimacEntity + ", catTipoTiendaEntity="
				+ catTipoTiendaEntity + ", activo=" + activo + ", fechaCreacion=" + fechaCreacion
				+ ", fechaModificacion=" + fechaModificacion + "]";
	}
	
	

}

package com.sodimac.cfdi.entity.fiscal.menu;

import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Objects;

import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.OneToMany;
import javax.persistence.PreUpdate;
import javax.persistence.Table;
import javax.persistence.Temporal;
import javax.persistence.TemporalType;

import org.hibernate.annotations.Cache;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.NaturalIdCache;

@Entity
@Table(name = "catmenu")
@NaturalIdCache
@Cache(usage = org.hibernate.annotations.CacheConcurrencyStrategy.READ_WRITE)
public class CatMenuEntity implements Serializable   {

	/**
	 * 
	 */
	private static final long serialVersionUID = -5044686655738798002L;

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	@Column(name = "id")
	private int id;
	
	@Column(name = "nombre")
	private String nombre;
	@Column(name = "descripcion")
	private String descripcion;
	@Column(name = "url")
	private String url;
	@Column(name = "idpadre")
	private int idpadre;
	@Column(name = "activo")
	private boolean activo;
	
	@Column(name = "fechaInicio")
	@CreationTimestamp
	@Temporal(TemporalType.TIMESTAMP)
	private java.util.Date fechaInicio;
	
	@Column(name = "fechaFin")
	@CreationTimestamp
	@Temporal(TemporalType.TIMESTAMP)
	private java.util.Date fechaFin;
	
	@Column(name = "idusuario")
	private int idusuario;
	
	@Column(name = "fechaCreacion")
	@CreationTimestamp
	@Temporal(TemporalType.TIMESTAMP)
	private java.util.Date fechaCreacion;
	
	@Column(name = "fechaActualizacion")
	@CreationTimestamp
	@Temporal(TemporalType.TIMESTAMP)
	private java.util.Date fechaActualizacion;
	
	@Column(name = "icon")
	private String icon;
	
    @OneToMany(
            mappedBy = "menu",
            cascade = CascadeType.ALL,
            orphanRemoval = true
        )
    private List<CatRolMenuEntity> rols = new ArrayList<>();
	
	public CatMenuEntity() {}

	@PreUpdate
	public void saveTime() {
	    this.fechaActualizacion = new Date();
	}
	
	public int getId() {
		return id;
	}

	public void setId(int id) {
		this.id = id;
	}

	public String getNombre() {
		return nombre;
	}

	public void setNombre(String nombre) {
		this.nombre = nombre;
	}

	public String getDescripcion() {
		return descripcion;
	}

	public void setDescripcion(String descripcion) {
		this.descripcion = descripcion;
	}

	public String getUrl() {
		return url;
	}

	public void setUrl(String url) {
		this.url = url;
	}

	public int getIdpadre() {
		return idpadre;
	}

	public void setIdpadre(int idpadre) {
		this.idpadre = idpadre;
	}

	public boolean isActivo() {
		return activo;
	}

	public void setActivo(boolean activo) {
		this.activo = activo;
	}

	public java.util.Date getFechaInicio() {
		return fechaInicio;
	}

	public void setFechaInicio(java.util.Date fechaInicio) {
		this.fechaInicio = fechaInicio;
	}

	public java.util.Date getFechaFin() {
		return fechaFin;
	}

	public void setFechaFin(java.util.Date fechaFin) {
		this.fechaFin = fechaFin;
	}

	public int getIdusuario() {
		return idusuario;
	}

	public void setIdusuario(int idusuario) {
		this.idusuario = idusuario;
	}

	public java.util.Date getFechaCreacion() {
		return fechaCreacion;
	}

	public void setFechaCreacion(java.util.Date fechaCreacion) {
		this.fechaCreacion = fechaCreacion;
	}



	public List<CatRolMenuEntity> getRols() {
		return rols;
	}

	public void setRols(List<CatRolMenuEntity> rols) {
		this.rols = rols;
	}
	
	
	public java.util.Date getFechaActualizacion() {
		return fechaActualizacion;
	}

	public void setFechaActualizacion(java.util.Date fechaActualizacion) {
		this.fechaActualizacion = fechaActualizacion;
	}

	public String getIcon() {
		return icon;
	}

	public void setIcon(String icon) {
		this.icon = icon;
	}

	@Override
	public String toString() {
		return "CatMenuEntity [id=" + id + ", nombre=" + nombre + ", descripcion=" + descripcion + ", url=" + url
				+ ", idpadre=" + idpadre + ", activo=" + activo + ", fechaInicio=" + fechaInicio + ", fechaFin="
				+ fechaFin + ", idusuario=" + idusuario + ", fechaCreacion=" + fechaCreacion + ", fechaActualizacion="
				+ fechaActualizacion + ", icon=" + icon + ", rols=" + rols + "]";
	}

	@Override
	public int hashCode() {
		return Objects.hash(activo, descripcion, fechaActualizacion, fechaCreacion, fechaFin, fechaInicio, icon, id,
				idpadre, idusuario, nombre, rols, url);
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		CatMenuEntity other = (CatMenuEntity) obj;
		return activo == other.activo && Objects.equals(descripcion, other.descripcion)
				&& Objects.equals(fechaActualizacion, other.fechaActualizacion)
				&& Objects.equals(fechaCreacion, other.fechaCreacion) && Objects.equals(fechaFin, other.fechaFin)
				&& Objects.equals(fechaInicio, other.fechaInicio) && Objects.equals(icon, other.icon) && id == other.id
				&& idpadre == other.idpadre && idusuario == other.idusuario && Objects.equals(nombre, other.nombre)
				&& Objects.equals(rols, other.rols) && Objects.equals(url, other.url);
	}


	
}

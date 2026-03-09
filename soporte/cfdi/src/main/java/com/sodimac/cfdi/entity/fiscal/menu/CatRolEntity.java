package com.sodimac.cfdi.entity.fiscal.menu;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.OneToMany;
import javax.persistence.Table;
import javax.persistence.Temporal;
import javax.persistence.TemporalType;
import javax.persistence.JoinColumn;

import org.hibernate.annotations.CreationTimestamp;

@Entity
@Table(name = "catrol")
public class CatRolEntity implements Serializable {

	/**
	 * 
	 */
	private static final long serialVersionUID = -6905869873024567058L;

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	@Column(name = "id")
	private int id;
	
	@Column(name = "nombre")
	private String nombre;
	@Column(name = "descripcion")
	private String descripcion;
	@Column(name = "activo")
	private boolean activo;
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
	
    @OneToMany(
            mappedBy = "rol",
            cascade = CascadeType.ALL,
            orphanRemoval = true     
        )
     private List<CatRolMenuEntity> menus = new ArrayList<>();
    
    @OneToMany(
            mappedBy = "rol",
            cascade = CascadeType.ALL,
            orphanRemoval = true     
        )
     private List<CatPerfilRolEntity> pefiles = new ArrayList<>();
	
	public CatRolEntity() {}

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

	public boolean isActivo() {
		return activo;
	}

	public void setActivo(boolean activo) {
		this.activo = activo;
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

	public java.util.Date getFechaActualización() {
		return fechaActualizacion;
	}

	public void setFechaActualización(java.util.Date fechaActualización) {
		this.fechaActualizacion = fechaActualización;
	}

	public List<CatRolMenuEntity> getMenus() {
		return menus;
	}

	public void setMenus(List<CatRolMenuEntity> menus) {
		this.menus = menus;
	}
	

	public List<CatPerfilRolEntity> getPefiles() {
		return pefiles;
	}

	public void setPefiles(List<CatPerfilRolEntity> pefiles) {
		this.pefiles = pefiles;
	}

	@Override
	public String toString() {
		return "CatRolEntity [id=" + id + ", nombre=" + nombre + ", descripcion=" + descripcion + ", activo=" + activo
				+ ", idusuario=" + idusuario + ", fechaCreacion=" + fechaCreacion + ", fechaActualización="
				+ fechaActualizacion + ", menus=" + menus + ", pefiles=" + pefiles + "]";
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + (activo ? 1231 : 1237);
		result = prime * result + ((descripcion == null) ? 0 : descripcion.hashCode());
		result = prime * result + ((fechaActualizacion == null) ? 0 : fechaActualizacion.hashCode());
		result = prime * result + ((fechaCreacion == null) ? 0 : fechaCreacion.hashCode());
		result = prime * result + id;
		result = prime * result + idusuario;
		result = prime * result + ((menus == null) ? 0 : menus.hashCode());
		result = prime * result + ((nombre == null) ? 0 : nombre.hashCode());
		result = prime * result + ((pefiles == null) ? 0 : pefiles.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		CatRolEntity other = (CatRolEntity) obj;
		if (activo != other.activo)
			return false;
		if (descripcion == null) {
			if (other.descripcion != null)
				return false;
		} else if (!descripcion.equals(other.descripcion))
			return false;
		if (fechaActualizacion == null) {
			if (other.fechaActualizacion != null)
				return false;
		} else if (!fechaActualizacion.equals(other.fechaActualizacion))
			return false;
		if (fechaCreacion == null) {
			if (other.fechaCreacion != null)
				return false;
		} else if (!fechaCreacion.equals(other.fechaCreacion))
			return false;
		if (id != other.id)
			return false;
		if (idusuario != other.idusuario)
			return false;
		if (menus == null) {
			if (other.menus != null)
				return false;
		} else if (!menus.equals(other.menus))
			return false;
		if (nombre == null) {
			if (other.nombre != null)
				return false;
		} else if (!nombre.equals(other.nombre))
			return false;
		if (pefiles == null) {
			if (other.pefiles != null)
				return false;
		} else if (!pefiles.equals(other.pefiles))
			return false;
		return true;
	}

	
	


}

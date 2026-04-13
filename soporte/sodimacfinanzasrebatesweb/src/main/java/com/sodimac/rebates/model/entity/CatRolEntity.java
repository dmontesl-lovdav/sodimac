package com.sodimac.rebates.model.entity;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.OneToMany;
import javax.persistence.Table;

@Entity
@Table(name = "CatRol")
public class CatRolEntity implements Serializable {

	private static final long serialVersionUID = -6905869873024567058L;

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	@Column(name = "id")
	private int id;
	
	@Column(name = "nombre")
	private String nombre;
	
	@Column(name = "prioridad")
	private String prioridad;
	
	@Column(name = "activo")
	private boolean activo;
	
	@Column(name = "usuarioCreacion")
	private Integer usuarioCreacion;
	
	@Column(name = "fechaCreacion")
	private Date fechaCreacion;
	
	@Column(name = "usuarioActualizacion")
	private Integer usuarioActualizacion;
	
	@Column(name = "fechaActualizacion")
	private Date fechaActualizacion;
	
    @OneToMany(
            mappedBy = "rol",
            cascade = CascadeType.ALL,
            orphanRemoval = true     
        )
     private List<CatRolPermisoEntity> permisos = new ArrayList<>();
    
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

	public String getPrioridad() {
		return prioridad;
	}

	public void setPrioridad(String prioridad) {
		this.prioridad = prioridad;
	}

	public boolean isActivo() {
		return activo;
	}

	public void setActivo(boolean activo) {
		this.activo = activo;
	}

	public Integer getUsuarioActualizacion() {
		return usuarioActualizacion;
	}

	public void setUsuarioActualizacionn(Integer usuarioActualizacion) {
		this.usuarioActualizacion = usuarioActualizacion;
	}

	public Date getFechaCreacion() {
		return fechaCreacion;
	}

	public void setFechaCreacion(Date fechaCreacion) {
		this.fechaCreacion = fechaCreacion;
	}
	
	public Integer getUsuarioCreacion() {
		return usuarioCreacion;
	}

	public void setUsuarioCreacion(Integer usuarioCreacion) {
		this.usuarioCreacion = usuarioCreacion;
	}

	public Date getFechaActualización() {
		return fechaActualizacion;
	}

	public void setFechaActualización(Date fechaActualización) {
		this.fechaActualizacion = fechaActualización;
	}

	public List<CatRolPermisoEntity> getPermisos() {
		return permisos;
	}

	public void setPermisos(List<CatRolPermisoEntity> permisos) {
		this.permisos = permisos;
	}
	

	public List<CatPerfilRolEntity> getPefiles() {
		return pefiles;
	}

	public void setPefiles(List<CatPerfilRolEntity> pefiles) {
		this.pefiles = pefiles;
	}
}

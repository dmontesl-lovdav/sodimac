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
@Table(name = "CatPerfil")
public class CatPerfilEntity implements Serializable {

	private static final long serialVersionUID = 3305932195909337849L;
	
	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	@Column(name = "id")
	private int id;
	
	@Column(name = "nombre")
	private String nombre;
	
	
	@Column(name = "activo")
	private boolean activo;
	
	@Column(name = "usuarioCreacion")
	private int usuarioCreacion;
	
	@Column(name = "fechaCreacion")
	private Date fechaCreacion;
	
	@Column(name = "fechaActualizacion")
	private Date fechaActualizacion;

    
    @OneToMany(
            mappedBy = "perfil",
            cascade = CascadeType.ALL,
            orphanRemoval = true     
        )
     private List<CatPerfilRolEntity> pefiles = new ArrayList<>();
    
    @OneToMany(
            mappedBy = "perfil",
            cascade = CascadeType.ALL,
            orphanRemoval = true     
        )
     private List<CatUsuarioPerfilEntity> usuarios = new ArrayList<>();
    
    public CatPerfilEntity() {}

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

	public boolean isActivo() {
		return activo;
	}

	public void setActivo(boolean activo) {
		this.activo = activo;
	}

	public int getUsuarioCreacion() {
		return usuarioCreacion;
	}

	public void setUsuarioCreacion(int usuarioCreacion) {
		this.usuarioCreacion = usuarioCreacion;
	}

	public Date getFechaCreacion() {
		return fechaCreacion;
	}

	public void setFechaCreacion(Date fechaCreacion) {
		this.fechaCreacion = fechaCreacion;
	}



	public List<CatPerfilRolEntity> getPefiles() {
		return pefiles;
	}

	public void setPefiles(List<CatPerfilRolEntity> pefiles) {
		this.pefiles = pefiles;
	}

	public Date getFechaActualizacion() {
		return fechaActualizacion;
	}

	public void setFechaActualizacion(Date fechaActualizacion) {
		this.fechaActualizacion = fechaActualizacion;
	}

	public List<CatUsuarioPerfilEntity> getUsuarios() {
		return usuarios;
	}

	public void setUsuarios(List<CatUsuarioPerfilEntity> usuarios) {
		this.usuarios = usuarios;
	}
}

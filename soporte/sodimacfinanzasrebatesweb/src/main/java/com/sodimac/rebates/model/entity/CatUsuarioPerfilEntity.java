package com.sodimac.rebates.model.entity;

import java.io.Serializable;
import java.util.Date;

import javax.persistence.Column;
import javax.persistence.EmbeddedId;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
//import javax.persistence.MapsId;
import javax.persistence.Table;

import com.sodimac.rebates.model.Usuario;

@Entity
@Table(name = "catusuarioperfil")
public class CatUsuarioPerfilEntity implements Serializable {

	/**
	 * 
	 */
	private static final long serialVersionUID = 6396373429912701869L;
	
	@EmbeddedId
	private CatUsuarioPerfilId catUsuarioPerfilPk;
	
    @ManyToOne(fetch = FetchType.LAZY)
    //@MapsId("idusuario")
    @JoinColumn(name = "idusuario", referencedColumnName = "id", insertable = false, updatable = false)
    private Usuario usuario;
    
    @ManyToOne(fetch = FetchType.LAZY)
    //@MapsId("idperfil")
    @JoinColumn(name = "idperfil", referencedColumnName = "id", insertable = false, updatable = false)
    private CatPerfilEntity perfil;
    
	@Column(name = "fechaCreacion")
	private Date fechaCreacion;
	
	@Column(name = "fechaActualizacion")
	private Date fechaActualizacion;
	
	public CatUsuarioPerfilEntity() {}
	
	public CatUsuarioPerfilEntity(Usuario usuario, CatPerfilEntity perfil) {
		this.usuario = usuario;
		this.perfil = perfil;
		this.catUsuarioPerfilPk = new CatUsuarioPerfilId(usuario.getId(), perfil.getId());
		
	}

	public CatUsuarioPerfilId getCatUsuarioPerfilPk() {
		return catUsuarioPerfilPk;
	}

	public void setCatUsuarioPerfilPk(CatUsuarioPerfilId catUsuarioPerfilPk) {
		this.catUsuarioPerfilPk = catUsuarioPerfilPk;
	}

	public Usuario getUsuario() {
		return usuario;
	}

	public void setUsuario(Usuario usuario) {
		this.usuario = usuario;
	}

	public CatPerfilEntity getPerfil() {
		return perfil;
	}

	public void setPerfil(CatPerfilEntity perfil) {
		this.perfil = perfil;
	}

	public Date getFechaCreacion() {
		return fechaCreacion;
	}

	public void setFechaCreacion(Date fechaCreacion) {
		this.fechaCreacion = fechaCreacion;
	}

	public Date getFechaActualizacion() {
		return fechaActualizacion;
	}

	public void setFechaActualizacion(Date fechaActualizacion) {
		this.fechaActualizacion = fechaActualizacion;
	}

}

package com.sodimac.cfdi.entity.fiscal.menu;

import java.io.Serializable;

import javax.persistence.Column;
import javax.persistence.EmbeddedId;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.ManyToOne;
import javax.persistence.MapsId;
import javax.persistence.Table;
import javax.persistence.Temporal;
import javax.persistence.TemporalType;

import org.hibernate.annotations.CreationTimestamp;

import com.sodimac.cfdi.entity.fiscal.UsuariosEntity;

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
    @MapsId("idusuario")
    private UsuariosEntity usuario;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @MapsId("idperfil")
    private CatPerfilEntity perfil;
    
	@Column(name = "fechaCreacion")
	@CreationTimestamp
	@Temporal(TemporalType.TIMESTAMP)
	private java.util.Date fechaCreacion;
	
	@Column(name = "fechaActualizacion")
	@CreationTimestamp
	@Temporal(TemporalType.TIMESTAMP)
	private java.util.Date fechaActualizacion;
	
	public CatUsuarioPerfilEntity() {}
	
	public CatUsuarioPerfilEntity(UsuariosEntity usuario, CatPerfilEntity perfil) {
		this.usuario = usuario;
		this.perfil = perfil;
		this.catUsuarioPerfilPk = new CatUsuarioPerfilId(usuario.getIdUsuario(), perfil.getId());
		
	}

	public CatUsuarioPerfilId getCatUsuarioPerfilPk() {
		return catUsuarioPerfilPk;
	}

	public void setCatUsuarioPerfilPk(CatUsuarioPerfilId catUsuarioPerfilPk) {
		this.catUsuarioPerfilPk = catUsuarioPerfilPk;
	}

	public UsuariosEntity getUsuario() {
		return usuario;
	}

	public void setUsuario(UsuariosEntity usuario) {
		this.usuario = usuario;
	}

	public CatPerfilEntity getPerfil() {
		return perfil;
	}

	public void setPerfil(CatPerfilEntity perfil) {
		this.perfil = perfil;
	}

	public java.util.Date getFechaCreacion() {
		return fechaCreacion;
	}

	public void setFechaCreacion(java.util.Date fechaCreacion) {
		this.fechaCreacion = fechaCreacion;
	}

	public java.util.Date getFechaActualizacion() {
		return fechaActualizacion;
	}

	public void setFechaActualizacion(java.util.Date fechaActualizacion) {
		this.fechaActualizacion = fechaActualizacion;
	}

	@Override
	public String toString() {
		return "CatUsuarioPerfilEntity [catUsuarioPerfilPk=" + catUsuarioPerfilPk + ", usuario=" + usuario + ", perfil="
				+ perfil + ", fechaCreacion=" + fechaCreacion + ", fechaActualizacion=" + fechaActualizacion + "]";
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((catUsuarioPerfilPk == null) ? 0 : catUsuarioPerfilPk.hashCode());
		result = prime * result + ((fechaActualizacion == null) ? 0 : fechaActualizacion.hashCode());
		result = prime * result + ((fechaCreacion == null) ? 0 : fechaCreacion.hashCode());
		result = prime * result + ((perfil == null) ? 0 : perfil.hashCode());
		result = prime * result + ((usuario == null) ? 0 : usuario.hashCode());
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
		CatUsuarioPerfilEntity other = (CatUsuarioPerfilEntity) obj;
		if (catUsuarioPerfilPk == null) {
			if (other.catUsuarioPerfilPk != null)
				return false;
		} else if (!catUsuarioPerfilPk.equals(other.catUsuarioPerfilPk))
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
		if (perfil == null) {
			if (other.perfil != null)
				return false;
		} else if (!perfil.equals(other.perfil))
			return false;
		if (usuario == null) {
			if (other.usuario != null)
				return false;
		} else if (!usuario.equals(other.usuario))
			return false;
		return true;
	}
	
	

}

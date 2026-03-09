package com.sodimac.cfdi.entity.fiscal;

import java.io.Serializable;
import java.util.ArrayList;
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

import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.Type;


import com.sodimac.cfdi.entity.fiscal.menu.CatUsuarioPerfilEntity;


@Entity
@Table(name = "usuarios")
public class UsuariosEntity implements Serializable {

	/**
	 * 
	 */
	private static final long serialVersionUID = 522196897660135494L;

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	@Column(name = "idUsuario")
	private Integer idUsuario;

	@Column(name = "usuario")
	private String usuario;

	@Column(name = "password")
	private String password;

	@Column(name = "primerNombre")
	private String primerNombre;
	
	@Column(name = "segundoNombre")
	private String segundoNombre;
	
	@Column(name = "apellidoPaterno")
	private String apellidoPaterno;
	
	@Column(name = "apellidoMaterno")
	private String apellidoMaterno;

	@Column(name = "idRol")
	private Integer idRol;

	@Column(name = "activo", nullable = false, columnDefinition = "BIT", length = 1)
	@Type(type = "org.hibernate.type.NumericBooleanType")
	private boolean activo = true;
	
	@Column(name = "cambiarContrasenia", nullable = false, columnDefinition = "BIT", length = 1)
	@Type(type = "org.hibernate.type.NumericBooleanType")
	private boolean cambiarContrasenia = true;

	@Column(name = "fechaCreacion")
	@CreationTimestamp
	@Temporal(TemporalType.TIMESTAMP)
	private java.util.Date fechaCreacion;

	@Column(name = "fechaModificacion")
	private java.util.Date fechaModificacion;
	
    @OneToMany(
            mappedBy = "usuario",
            cascade = CascadeType.ALL,
            orphanRemoval = true     
        )
     private List<CatUsuarioPerfilEntity> perfiles = new ArrayList<>();
	

	public UsuariosEntity() {}


	public Integer getIdUsuario() {
		return idUsuario;
	}


	public void setIdUsuario(Integer idUsuario) {
		this.idUsuario = idUsuario;
	}


	public String getUsuario() {
		return usuario;
	}


	public void setUsuario(String usuario) {
		this.usuario = usuario;
	}


	public String getPassword() {
		return password;
	}


	public void setPassword(String password) {
		this.password = password;
	}


	public String getPrimerNombre() {
		return primerNombre;
	}


	public void setPrimerNombre(String primerNombre) {
		this.primerNombre = primerNombre;
	}


	public String getSegundoNombre() {
		return segundoNombre;
	}


	public void setSegundoNombre(String segundoNombre) {
		this.segundoNombre = segundoNombre;
	}


	public String getApellidoPaterno() {
		return apellidoPaterno;
	}


	public void setApellidoPaterno(String apellidoPaterno) {
		this.apellidoPaterno = apellidoPaterno;
	}


	public String getApellidoMaterno() {
		return apellidoMaterno;
	}


	public void setApellidoMaterno(String apellidoMaterno) {
		this.apellidoMaterno = apellidoMaterno;
	}


	public Integer getIdRol() {
		return idRol;
	}


	public void setIdRol(Integer idRol) {
		this.idRol = idRol;
	}


	public boolean isActivo() {
		return activo;
	}


	public void setActivo(boolean activo) {
		this.activo = activo;
	}


	public boolean isCambiarContrasenia() {
		return cambiarContrasenia;
	}


	public void setCambiarContrasenia(boolean cambiarContrasenia) {
		this.cambiarContrasenia = cambiarContrasenia;
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


	public List<CatUsuarioPerfilEntity> getPerfiles() {
		return perfiles;
	}


	public void setPerfiles(List<CatUsuarioPerfilEntity> perfiles) {
		this.perfiles = perfiles;
	}


	@Override
	public String toString() {
		return "UsuariosEntity [idUsuario=" + idUsuario + ", usuario=" + usuario + ", password=" + password
				+ ", primerNombre=" + primerNombre + ", segundoNombre=" + segundoNombre + ", apellidoPaterno="
				+ apellidoPaterno + ", apellidoMaterno=" + apellidoMaterno + ", idRol=" + idRol + ", activo=" + activo
				+ ", cambiarContrasenia=" + cambiarContrasenia + ", fechaCreacion=" + fechaCreacion
				+ ", fechaModificacion=" + fechaModificacion + ", perfiles=" + perfiles + "]";
	}


	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + (activo ? 1231 : 1237);
		result = prime * result + ((apellidoMaterno == null) ? 0 : apellidoMaterno.hashCode());
		result = prime * result + ((apellidoPaterno == null) ? 0 : apellidoPaterno.hashCode());
		result = prime * result + (cambiarContrasenia ? 1231 : 1237);
		result = prime * result + ((fechaCreacion == null) ? 0 : fechaCreacion.hashCode());
		result = prime * result + ((fechaModificacion == null) ? 0 : fechaModificacion.hashCode());
		result = prime * result + ((idRol == null) ? 0 : idRol.hashCode());
		result = prime * result + ((idUsuario == null) ? 0 : idUsuario.hashCode());
		result = prime * result + ((password == null) ? 0 : password.hashCode());
		result = prime * result + ((perfiles == null) ? 0 : perfiles.hashCode());
		result = prime * result + ((primerNombre == null) ? 0 : primerNombre.hashCode());
		result = prime * result + ((segundoNombre == null) ? 0 : segundoNombre.hashCode());
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
		UsuariosEntity other = (UsuariosEntity) obj;
		if (activo != other.activo)
			return false;
		if (apellidoMaterno == null) {
			if (other.apellidoMaterno != null)
				return false;
		} else if (!apellidoMaterno.equals(other.apellidoMaterno))
			return false;
		if (apellidoPaterno == null) {
			if (other.apellidoPaterno != null)
				return false;
		} else if (!apellidoPaterno.equals(other.apellidoPaterno))
			return false;
		if (cambiarContrasenia != other.cambiarContrasenia)
			return false;
		if (fechaCreacion == null) {
			if (other.fechaCreacion != null)
				return false;
		} else if (!fechaCreacion.equals(other.fechaCreacion))
			return false;
		if (fechaModificacion == null) {
			if (other.fechaModificacion != null)
				return false;
		} else if (!fechaModificacion.equals(other.fechaModificacion))
			return false;
		if (idRol == null) {
			if (other.idRol != null)
				return false;
		} else if (!idRol.equals(other.idRol))
			return false;
		if (idUsuario == null) {
			if (other.idUsuario != null)
				return false;
		} else if (!idUsuario.equals(other.idUsuario))
			return false;
		if (password == null) {
			if (other.password != null)
				return false;
		} else if (!password.equals(other.password))
			return false;
		if (perfiles == null) {
			if (other.perfiles != null)
				return false;
		} else if (!perfiles.equals(other.perfiles))
			return false;
		if (primerNombre == null) {
			if (other.primerNombre != null)
				return false;
		} else if (!primerNombre.equals(other.primerNombre))
			return false;
		if (segundoNombre == null) {
			if (other.segundoNombre != null)
				return false;
		} else if (!segundoNombre.equals(other.segundoNombre))
			return false;
		if (usuario == null) {
			if (other.usuario != null)
				return false;
		} else if (!usuario.equals(other.usuario))
			return false;
		return true;
	}

	
}

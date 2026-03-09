package com.sodimac.cfdi.entity.fiscal.menu;

import java.io.Serializable;

import javax.persistence.Column;
import javax.persistence.Embeddable;

@Embeddable
public class CatUsuarioPerfilId implements Serializable {

	/**
	 * 
	 */
	private static final long serialVersionUID = -831950243417042670L;
	
    @Column(name = "idusuario")
    private int usuario;
 
    @Column(name = "idperfil")
    private int perfil;
    
    public CatUsuarioPerfilId() {}
    
    public CatUsuarioPerfilId(int usuario, int perfil) {
    	this.usuario = usuario;
    	this.perfil = perfil;	
    }

	public int getUsuario() {
		return usuario;
	}

	public void setUsuario(int usuario) {
		this.usuario = usuario;
	}

	public int getPerfil() {
		return perfil;
	}

	public void setPerfil(int perfil) {
		this.perfil = perfil;
	}

	@Override
	public String toString() {
		return "CatUsuarioPerfilId [usuario=" + usuario + ", perfil=" + perfil + "]";
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + perfil;
		result = prime * result + usuario;
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
		CatUsuarioPerfilId other = (CatUsuarioPerfilId) obj;
		if (perfil != other.perfil)
			return false;
		if (usuario != other.usuario)
			return false;
		return true;
	}
    
    

}

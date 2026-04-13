package com.sodimac.rebates.model.entity;

import java.io.Serializable;
import javax.persistence.Column;
import javax.persistence.Embeddable;

/**
 *
 * @author david.montes
 */
@Embeddable
public class CatUsuarioPerfilEntityPK implements Serializable {

    /**
	 * 
	 */
	private static final long serialVersionUID = 5869810910871625534L;

	@Column(name = "idusuario")
    private int idusuario;
    
    @Column(name = "idperfil")
    private int idperfil;

    public CatUsuarioPerfilEntityPK() {
    }

    public CatUsuarioPerfilEntityPK(int idusuario, int idperfil) {
        this.idusuario = idusuario;
        this.idperfil = idperfil;
    }

    public int getIdusuario() {
        return idusuario;
    }

    public void setIdusuario(int idusuario) {
        this.idusuario = idusuario;
    }

    public int getIdperfil() {
        return idperfil;
    }

    public void setIdperfil(int idperfil) {
        this.idperfil = idperfil;
    }    
}

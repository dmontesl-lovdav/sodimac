package com.sodimac.rebates.model.entity;

import java.io.Serializable;
import javax.persistence.Column;
import javax.persistence.Embeddable;

/**
 *
 * @author david.montes
 */
@Embeddable
public class CatPerfilExclusionEntityPK implements Serializable {

    /**
	 * 
	 */
	private static final long serialVersionUID = 2063154534395686309L;

	@Column(name = "IdCatPerfil")
    private int idCatPerfil;
    
    @Column(name = "IdCatTipoExclusion")
    private int idCatTipoExclusion;

    public CatPerfilExclusionEntityPK() {
    }

    public CatPerfilExclusionEntityPK(int idCatPerfil, int idCatTipoExclusion) {
        this.idCatPerfil = idCatPerfil;
        this.idCatTipoExclusion = idCatTipoExclusion;
    }

    public int getIdCatPerfil() {
        return idCatPerfil;
    }

    public void setIdCatPerfil(int idCatPerfil) {
        this.idCatPerfil = idCatPerfil;
    }

    public int getIdCatTipoExclusion() {
        return idCatTipoExclusion;
    }

    public void setIdCatTipoExclusion(int idCatTipoExclusion) {
        this.idCatTipoExclusion = idCatTipoExclusion;
    }    
    
}

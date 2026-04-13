package com.sodimac.rebates.model.entity;

import java.io.Serializable;
import javax.persistence.Column;
import javax.persistence.Embeddable;

/**
 *
 * @author david.montes
 */
@Embeddable
public class CatPerfilRebateEntityPK implements Serializable {

    /**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	@Column(name = "IdCatPerfil")
    private int idCatPerfil;
    
    @Column(name = "IdCatTipoRebate")
    private int idCatTipoRebate;

    public CatPerfilRebateEntityPK() {
    }

    public CatPerfilRebateEntityPK(int idCatPerfil, int idCatTipoRebate) {
        this.idCatPerfil = idCatPerfil;
        this.idCatTipoRebate = idCatTipoRebate;
    }

    public int getIdCatPerfil() {
        return idCatPerfil;
    }

    public void setIdCatPerfil(int idCatPerfil) {
        this.idCatPerfil = idCatPerfil;
    }

    public int getIdCatTipoRebate() {
        return idCatTipoRebate;
    }

    public void setIdCatTipoRebate(int idCatTipoRebate) {
        this.idCatTipoRebate = idCatTipoRebate;
    }
    
}

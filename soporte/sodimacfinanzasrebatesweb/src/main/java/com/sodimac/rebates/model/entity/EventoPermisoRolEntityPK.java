package com.sodimac.rebates.model.entity;

import java.io.Serializable;
import javax.persistence.Column;
import javax.persistence.Embeddable;

/**
 *
 * @author david.montes
 */
@Embeddable
public class EventoPermisoRolEntityPK implements Serializable {

    /**
	 * 
	 */
	private static final long serialVersionUID = -5889206419153039221L;
	@Column(name = "IdCatEvento")
    private int idCatEvento;
    
	@Column(name = "IdPermiso")
    private int idPermiso;
    
	@Column(name = "IdRol")
    private int idRol;

    public EventoPermisoRolEntityPK() {
    }

    public EventoPermisoRolEntityPK(int idCatEvento, int idPermiso, int idRol) {
        this.idCatEvento = idCatEvento;
        this.idPermiso = idPermiso;
        this.idRol = idRol;
    }

    public int getIdCatEvento() {
        return idCatEvento;
    }

    public void setIdCatEvento(int idCatEvento) {
        this.idCatEvento = idCatEvento;
    }

    public int getIdPermiso() {
        return idPermiso;
    }

    public void setIdPermiso(int idPermiso) {
        this.idPermiso = idPermiso;
    }

    public int getIdRol() {
        return idRol;
    }

    public void setIdRol(int idRol) {
        this.idRol = idRol;
    }

    @Override
    public int hashCode() {
        int hash = 0;
        hash += (int) idCatEvento;
        hash += (int) idPermiso;
        hash += (int) idRol;
        return hash;
    }

    @Override
    public boolean equals(Object object) {
        if (!(object instanceof EventoPermisoRolEntityPK)) {
            return false;
        }
        EventoPermisoRolEntityPK other = (EventoPermisoRolEntityPK) object;
        if (this.idCatEvento != other.idCatEvento) {
            return false;
        }
        if (this.idPermiso != other.idPermiso) {
            return false;
        }
        if (this.idRol != other.idRol) {
            return false;
        }
        return true;
    }

    @Override
    public String toString() {
        return "com.sodimac.rebates.model.entity.EventoPermisoRolEntityPK[ idCatEvento=" + idCatEvento + ", idPermiso=" + idPermiso + ", idRol=" + idRol + " ]";
    }
    
}

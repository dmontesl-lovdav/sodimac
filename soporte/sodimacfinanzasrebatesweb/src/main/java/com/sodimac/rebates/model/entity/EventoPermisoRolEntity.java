package com.sodimac.rebates.model.entity;

import java.io.Serializable;
import java.util.Date;

import javax.persistence.Column;
import javax.persistence.EmbeddedId;
import javax.persistence.Entity;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.Table;
import javax.persistence.Temporal;
import javax.persistence.TemporalType;

/**
 *
 * @author david.montes
 */
@Entity
@Table(name = "EventoPermisoRol")
public class EventoPermisoRolEntity implements Serializable {

    private static final long serialVersionUID = 1L;
    @EmbeddedId
    protected EventoPermisoRolEntityPK eventoPermisoRolEntityPK;
    @Column(name = "Activo")
    private boolean activo;
    
    @Column(name = "FechaRegistro")
    @Temporal(TemporalType.TIMESTAMP)
    private Date fechaRegistro;
    
    @JoinColumn(name = "IdCatEvento", referencedColumnName = "IdCatEvento", insertable = false, updatable = false)
    @ManyToOne(optional = false)
    private CatEventoEntity catEventoEntity;
    
    @JoinColumn(name = "IdPermiso", referencedColumnName = "idpermiso", insertable = false, updatable = false)
    @ManyToOne(optional = false)
    private CatPermisoEntity catPermisoEntity;
    
    @JoinColumn(name = "IdRol", referencedColumnName = "id", insertable = false, updatable = false)
    @ManyToOne(optional = false)
    private CatRolEntity catRolEntity;

    public EventoPermisoRolEntity() {
    }

    public EventoPermisoRolEntity(EventoPermisoRolEntityPK eventoPermisoRolEntityPK) {
        this.eventoPermisoRolEntityPK = eventoPermisoRolEntityPK;
    }

    public EventoPermisoRolEntity(EventoPermisoRolEntityPK eventoPermisoRolEntityPK, boolean activo, Date fechaRegistro) {
        this.eventoPermisoRolEntityPK = eventoPermisoRolEntityPK;
        this.activo = activo;
        this.fechaRegistro = fechaRegistro;
    }

    public EventoPermisoRolEntity(int idCatEvento, int idPermiso, int idRol) {
        this.eventoPermisoRolEntityPK = new EventoPermisoRolEntityPK(idCatEvento, idPermiso, idRol);
    }

    public EventoPermisoRolEntityPK getEventoPermisoRolEntityPK() {
        return eventoPermisoRolEntityPK;
    }

    public void setEventoPermisoRolEntityPK(EventoPermisoRolEntityPK eventoPermisoRolEntityPK) {
        this.eventoPermisoRolEntityPK = eventoPermisoRolEntityPK;
    }

    public boolean getActivo() {
        return activo;
    }

    public void setActivo(boolean activo) {
        this.activo = activo;
    }

    public Date getFechaRegistro() {
        return fechaRegistro;
    }

    public void setFechaRegistro(Date fechaRegistro) {
        this.fechaRegistro = fechaRegistro;
    }

    public CatEventoEntity getCatEventoEntity() {
        return catEventoEntity;
    }

    public void setCatEventoEntity(CatEventoEntity catEventoEntity) {
        this.catEventoEntity = catEventoEntity;
    }

    public CatPermisoEntity getCatPermisoEntity() {
        return catPermisoEntity;
    }

    public void setCatPermisoEntity(CatPermisoEntity catPermisoEntity) {
        this.catPermisoEntity = catPermisoEntity;
    }

    public CatRolEntity getCatRolEntity() {
        return catRolEntity;
    }

    public void setCatRolEntity(CatRolEntity catRolEntity) {
        this.catRolEntity = catRolEntity;
    }

    @Override
    public int hashCode() {
        int hash = 0;
        hash += (eventoPermisoRolEntityPK != null ? eventoPermisoRolEntityPK.hashCode() : 0);
        return hash;
    }

    @Override
    public boolean equals(Object object) {
        if (!(object instanceof EventoPermisoRolEntity)) {
            return false;
        }
        EventoPermisoRolEntity other = (EventoPermisoRolEntity) object;
        if ((this.eventoPermisoRolEntityPK == null && other.eventoPermisoRolEntityPK != null) || (this.eventoPermisoRolEntityPK != null && !this.eventoPermisoRolEntityPK.equals(other.eventoPermisoRolEntityPK))) {
            return false;
        }
        return true;
    }

    @Override
    public String toString() {
        return "com.sodimac.rebates.model.entity.EventoPermisoRolEntity[ eventoPermisoRolEntityPK=" + eventoPermisoRolEntityPK + " ]";
    }
    
}

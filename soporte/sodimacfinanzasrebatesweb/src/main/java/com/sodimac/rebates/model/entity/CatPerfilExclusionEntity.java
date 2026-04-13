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
@Table(name = "CatPerfilExclusion")
public class CatPerfilExclusionEntity implements Serializable {

    private static final long serialVersionUID = 1L;
    @EmbeddedId
    protected CatPerfilExclusionEntityPK catPerfilExclusionEntityPK;
    
    @Column(name = "FechaCreacion")
    @Temporal(TemporalType.TIMESTAMP)
    private Date fechaCreacion;
    
    @JoinColumn(name = "IdCatPerfil", referencedColumnName = "id", insertable = false, updatable = false)
    @ManyToOne(optional = false)
    private CatPerfilEntity catPerfilEntity;
    
    @JoinColumn(name = "IdCatTipoExclusion", referencedColumnName = "IdCatTipoExclusion", insertable = false, updatable = false)
    @ManyToOne(optional = false)
    
    private CatTipoExclusionEntity catTipoExclusionEntity;

    public CatPerfilExclusionEntity() {
    }

    public CatPerfilExclusionEntity(CatPerfilExclusionEntityPK catPerfilExclusionEntityPK) {
        this.catPerfilExclusionEntityPK = catPerfilExclusionEntityPK;
    }

    public CatPerfilExclusionEntity(CatPerfilExclusionEntityPK catPerfilExclusionEntityPK, Date fechaCreacion) {
        this.catPerfilExclusionEntityPK = catPerfilExclusionEntityPK;
        this.fechaCreacion = fechaCreacion;
    }

    public CatPerfilExclusionEntity(int idCatPerfil, int idCatTipoExclusion) {
        this.catPerfilExclusionEntityPK = new CatPerfilExclusionEntityPK(idCatPerfil, idCatTipoExclusion);
    }

    public CatPerfilExclusionEntityPK getCatPerfilExclusionEntityPK() {
        return catPerfilExclusionEntityPK;
    }

    public void setCatPerfilExclusionEntityPK(CatPerfilExclusionEntityPK catPerfilExclusionEntityPK) {
        this.catPerfilExclusionEntityPK = catPerfilExclusionEntityPK;
    }

    public Date getFechaCreacion() {
        return fechaCreacion;
    }

    public void setFechaCreacion(Date fechaCreacion) {
        this.fechaCreacion = fechaCreacion;
    }

    public CatPerfilEntity getCatPerfilEntity() {
        return catPerfilEntity;
    }

    public void setCatPerfilEntity(CatPerfilEntity catPerfilEntity) {
        this.catPerfilEntity = catPerfilEntity;
    }

    public CatTipoExclusionEntity getCatTipoExclusionEntity() {
        return catTipoExclusionEntity;
    }

    public void setCatTipoExclusionEntity(CatTipoExclusionEntity catTipoExclusionEntity) {
        this.catTipoExclusionEntity = catTipoExclusionEntity;
    }    
    
}

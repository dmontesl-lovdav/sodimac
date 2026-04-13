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

import com.sodimac.rebates.model.TipoRebate;

/**
 *
 * @author david.montes
 */
@Entity
@Table(name = "CatPerfilRebate")
public class CatPerfilRebateEntity implements Serializable {

    private static final long serialVersionUID = 1L;
    @EmbeddedId
    protected CatPerfilRebateEntityPK catPerfilRebateEntityPK;
    
    @Column(name = "FechaCreacion")
    @Temporal(TemporalType.TIMESTAMP)
    private Date fechaCreacion;
    
    @JoinColumn(name = "IdCatPerfil", referencedColumnName = "id", insertable = false, updatable = false)
    @ManyToOne(optional = false)
    private CatPerfilEntity catPerfilEntity;
    
    @JoinColumn(name = "IdCatTipoRebate", referencedColumnName = "IdCatTipoRebate", insertable = false, updatable = false)
    @ManyToOne(optional = false)
    private TipoRebate catTipoRebateEntity;

    public CatPerfilRebateEntity() {
    }

    public CatPerfilRebateEntity(CatPerfilRebateEntityPK catPerfilRebateEntityPK) {
        this.catPerfilRebateEntityPK = catPerfilRebateEntityPK;
    }

    public CatPerfilRebateEntity(CatPerfilRebateEntityPK catPerfilRebateEntityPK, Date fechaCreacion) {
        this.catPerfilRebateEntityPK = catPerfilRebateEntityPK;
        this.fechaCreacion = fechaCreacion;
    }

    public CatPerfilRebateEntity(int idCatPerfil, int idCatTipoRebate) {
        this.catPerfilRebateEntityPK = new CatPerfilRebateEntityPK(idCatPerfil, idCatTipoRebate);
    }

    public CatPerfilRebateEntityPK getCatPerfilRebateEntityPK() {
        return catPerfilRebateEntityPK;
    }

    public void setCatPerfilRebateEntityPK(CatPerfilRebateEntityPK catPerfilRebateEntityPK) {
        this.catPerfilRebateEntityPK = catPerfilRebateEntityPK;
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

    public TipoRebate getCatTipoRebateEntity() {
        return catTipoRebateEntity;
    }

    public void setCatTipoRebateEntity(TipoRebate catTipoRebateEntity) {
        this.catTipoRebateEntity = catTipoRebateEntity;
    }
    
}

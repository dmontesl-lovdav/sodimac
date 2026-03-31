package com.sodimac.bctfacturacion.entity.bct;

import java.io.Serializable;
import javax.persistence.Basic;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;

/**
 *
 * @author david.montes
 */
@Entity
@Table(name = "sw_cem.trx_points_sku")
public class TrxPointsSkuEntity implements Serializable {

    private static final long serialVersionUID = 1L;
    
    @Id
    @Basic(optional = false)
    @Column(name = "ID")
    private Integer id;
    
    @Column(name = "SKU")
    private Integer sku;
    
    @Column(name = "POINTS")
    private Integer points;

    public TrxPointsSkuEntity() {
    }

    public TrxPointsSkuEntity(Integer id) {
        this.id = id;
    }

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public Integer getSku() {
        return sku;
    }

    public void setSku(Integer sku) {
        this.sku = sku;
    }

    public Integer getPoints() {
        return points;
    }

    public void setPoints(Integer points) {
        this.points = points;
    }

    @Override
    public String toString() {
        return "com.sodimac.bctfacturacion.entity.bct.TrxPointsSkuEntity[ id=" + id + " ]";
    }
    
}

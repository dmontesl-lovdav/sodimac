/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.sodimac.aclaraciones.api.model.entity;

import jakarta.persistence.Column;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.MappedSuperclass;
import jakarta.persistence.Temporal;
import jakarta.persistence.TemporalType;
import java.util.Date;
import java.io.Serializable;

/**
 *
 * @author ggalvan
 */
@MappedSuperclass
public abstract class AbstractModel implements Serializable {

    private static final long serialVersionUID = 1977449229056579247L;

    public static final int TYPE_DEFAULT = 1;
    public static final int STATUS_DEFAULT = 1;

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    protected int id;
    @Column(nullable = false)
    protected int type;
    @Column(nullable = false)
    protected int status;
    @Column(nullable = false)
    protected boolean active;

    @Column(nullable = false)
    @Temporal(TemporalType.TIMESTAMP)
    protected Date creationTime;
    @Temporal(TemporalType.TIMESTAMP)
    protected Date updateTime;

    public AbstractModel() {
    }

    public AbstractModel(int id) {
        this.id = id;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public int getType() {
        return type;
    }

    public void setType(int type) {
        this.type = type;
    }

    public int getStatus() {
        return status;
    }

    public void setStatus(int status) {
        this.status = status;
    }

    public boolean isActive() {
        return active;
    }

    public void setActive(boolean active) {
        this.active = active;
    }

    public Date getCreationTime() {
        return creationTime;
    }

    public void setCreationTime(Date creationTime) {
        this.creationTime = creationTime;
    }

    public Date getUpdateTime() {
        return updateTime;
    }

    public void setUpdateTime(Date updateTime) {
        this.updateTime = updateTime;
    }

}

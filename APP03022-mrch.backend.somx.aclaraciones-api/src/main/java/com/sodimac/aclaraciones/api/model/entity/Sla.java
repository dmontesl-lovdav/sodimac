package com.sodimac.aclaraciones.api.model.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;

@Entity
public class Sla extends AbstractModel {

    public static final int STATUS_PUBLISHED = 1;
    public static final int STATUS_UNPUBLISHED = 2;

    @ManyToOne
    @JoinColumn(name = "businessUnit", nullable = false)
    private Catalog businessUnit;
    @ManyToOne
    @JoinColumn(name = "country", nullable = false)
    private Catalog country;
    @ManyToOne
    @JoinColumn(name = "module", nullable = false)
    private Catalog module;
    @ManyToOne
    @JoinColumn(name = "reason", nullable = false)
    private Catalog reason;
    @ManyToOne
    @JoinColumn(name = "priority", nullable = false)
    private Catalog priority;
    @ManyToOne
    @JoinColumn(name = "firstResponseLevel", nullable = false)
    private Catalog firstResponseLevel;
    @ManyToOne
    @JoinColumn(name = "resolutionLevel", nullable = false)
    private Catalog resolutionLevel;
    //
    @Column(nullable = false, length = 128)
    private String manager;

    public Sla() {
    }

    public Sla(int id) {
        super(id);
    }

    public Catalog getBusinessUnit() {
        return businessUnit;
    }

    public void setBusinessUnit(Catalog businessUnit) {
        this.businessUnit = businessUnit;
    }

    public Catalog getCountry() {
        return country;
    }

    public void setCountry(Catalog country) {
        this.country = country;
    }

    public Catalog getModule() {
        return module;
    }

    public void setModule(Catalog module) {
        this.module = module;
    }

    public Catalog getReason() {
        return reason;
    }

    public void setReason(Catalog reason) {
        this.reason = reason;
    }

    public Catalog getPriority() {
        return priority;
    }

    public void setPriority(Catalog priority) {
        this.priority = priority;
    }

    public Catalog getFirstResponseLevel() {
        return firstResponseLevel;
    }

    public void setFirstResponseLevel(Catalog firstResponseLevel) {
        this.firstResponseLevel = firstResponseLevel;
    }

    public Catalog getResolutionLevel() {
        return resolutionLevel;
    }

    public void setResolutionLevel(Catalog resolutionLevel) {
        this.resolutionLevel = resolutionLevel;
    }

    public String getManager() {
        return manager;
    }

    public void setManager(String manager) {
        this.manager = manager;
    }

}

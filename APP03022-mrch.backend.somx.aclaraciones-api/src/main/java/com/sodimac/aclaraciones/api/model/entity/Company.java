/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.sodimac.aclaraciones.api.model.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;

/**
 *
 * @author ggalvan
 */
@Entity
public class Company extends AbstractModel {

    private static final long serialVersionUID = -2582505515430214256L;

    @Column(nullable = false, length = 32)
    private String name;

    @Column(nullable = false, length = 32)
    private String rut;

    @ManyToOne
    @JoinColumn(name = "businessUnit", nullable = false)
    private Catalog businessUnit;

    @ManyToOne
    @JoinColumn(name = "country", nullable = false)
    private Catalog country;

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getRut() {
        return rut;
    }

    public void setRut(String rut) {
        this.rut = rut;
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

}

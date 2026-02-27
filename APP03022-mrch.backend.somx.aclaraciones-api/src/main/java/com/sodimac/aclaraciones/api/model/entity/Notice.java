/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.sodimac.aclaraciones.api.model.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;

/**
 *
 * @author ggalvan
 */
@Entity
@Table
public class Notice extends AbstractModel {

    private static final long serialVersionUID = 8959586847782725401L;

    public static final int STATUS_UNPUBLISHED = 1;
    public static final int STATUS_PUBLISHED = 2;
    //
    public static final int TYPE_DEFAULT = 0;
    //

    @Column(nullable = false, length = 32)
    private String name;

    @Column(nullable = false, length = 128)
    private String description;

    @Column(nullable = false, length = 256)
    private String link;

    @ManyToOne
    @JoinColumn(name = "businessUnit", nullable = false)
    private Catalog businessUnit;

    @ManyToOne
    @JoinColumn(name = "country", nullable = false)
    private Catalog country;

    @ManyToOne
    @JoinColumn(name = "position", nullable = false)
    private Catalog position;

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getLink() {
        return link;
    }

    public void setLink(String link) {
        this.link = link;
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

    public Catalog getPosition() {
        return position;
    }

    public void setPosition(Catalog position) {
        this.position = position;
    }

}

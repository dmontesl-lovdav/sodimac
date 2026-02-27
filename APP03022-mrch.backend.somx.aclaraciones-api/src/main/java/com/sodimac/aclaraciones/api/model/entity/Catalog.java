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
public class Catalog extends AbstractModel {

    private static final long serialVersionUID = 431662608790510079L;

    @Column(length = 64, nullable = false)
    private String description;

    @ManyToOne
    @JoinColumn
    private Catalog parent;

    // 🔥 NUEVO CAMPO: área del catálogo
    @Column(length = 128)
    private String area;

    public Catalog() {
        super();
    }

    public Catalog(int id) {
        super(id);
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public Catalog getParent() {
        return parent;
    }

    public void setParent(Catalog parent) {
        this.parent = parent;
    }

    // ---------- GETTERS / SETTERS NUEVOS ----------

    public String getArea() {
        return area;
    }

    public void setArea(String area) {
        this.area = area;
    }
}

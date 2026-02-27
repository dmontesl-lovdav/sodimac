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
public class Author extends AbstractModel {

    private static final long serialVersionUID = 1146744955090824341L;

    public static final int TYPE_REGULAR = 1;
    public static final int TYPE_OPERATOR = 2;

    @Column(nullable = false, length = 64, unique = true)
    private String email;
    @Column(nullable = false, length = 32)
    private String name;
    @ManyToOne
    @JoinColumn(name = "reason")
    private Catalog reason;

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public Catalog getReason() {
        return reason;
    }

    public void setReason(Catalog reason) {
        this.reason = reason;
    }

}

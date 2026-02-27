package com.sodimac.aclaraciones.api.model.entity;

import jakarta.persistence.*;

@Entity
@Table(name = "SHIPPING_STATUS")
public class ShippingStatus {

    @Id
    @Column(name = "ID")
    private Integer id;

    @Column(name = "DESCRIPTION", length = 64)
    private String description;

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }
}

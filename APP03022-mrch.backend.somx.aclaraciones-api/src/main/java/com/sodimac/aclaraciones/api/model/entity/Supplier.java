package com.sodimac.aclaraciones.api.model.entity;

import jakarta.persistence.*;

@Entity
@Table(name = "CATSUPPLIER")
public class Supplier {

    @Id
    @Column(name = "SUPPLIERNUMBER")
    private Long supplierNumber;

    @Column(name = "NAME", length = 128)
    private String name;

    public Long getSupplierNumber() {
        return supplierNumber;
    }

    public void setSupplierNumber(Long supplierNumber) {
        this.supplierNumber = supplierNumber;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }
}

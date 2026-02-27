package com.sodimac.aclaraciones.api.model.entity;

import jakarta.persistence.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
// en Postgres conviene minúsculas; sin "schema" usa el esquema por defecto
// (public)
@Table(name = "shippingguide")
public class ShippingGuide {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "idshippingguide")
    private Long id;

    @Column(name = "idpurchaseorder")
    private Long purchaseOrderId;

    @Column(name = "shippingguide", length = 64, nullable = false)
    private String guideNumber;

    @Column(name = "suppliernumber", nullable = false)
    private Long supplierNumber;

    /** FK opcional: no insert/update porque usamos el mismo campo suppliernumber */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "suppliernumber", insertable = false, updatable = false)
    private Supplier supplier;

    @Column(name = "licenseplate", length = 32)
    private String plate;

    @Column(name = "trailerplate", length = 32)
    private String trailerPlate;

    @Column(name = "origin", length = 128)
    private String origin;

    @Column(name = "deliverytype", length = 64)
    private String deliveryType;

    @Column(name = "totalamount", precision = 18, scale = 2)
    private BigDecimal amount;

    @Column(name = "deliverydate")
    private LocalDate deliveryDate;

    @Column(name = "sentdate")
    private LocalDate shippingDate;

    @Lob
    @Column(name = "cartaportexml")
    private String cartaPorteXml;

    @Lob
    @Column(name = "cartaportecsv")
    private String cartaPorteCsv;

    @Column(name = "comments", length = 2000)
    private String comments;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "status")
    private ShippingStatus status;

    @Column(name = "idusercreated")
    private Long userCreatedId;

    @CreationTimestamp
    @Column(name = "createdat", updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "updatedat")
    private LocalDateTime updatedAt;

    /* Getters & Setters */

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Long getPurchaseOrderId() {
        return purchaseOrderId;
    }

    public void setPurchaseOrderId(Long purchaseOrderId) {
        this.purchaseOrderId = purchaseOrderId;
    }

    public String getGuideNumber() {
        return guideNumber;
    }

    public void setGuideNumber(String guideNumber) {
        this.guideNumber = guideNumber;
    }

    public Long getSupplierNumber() {
        return supplierNumber;
    }

    public void setSupplierNumber(Long supplierNumber) {
        this.supplierNumber = supplierNumber;
    }

    public Supplier getSupplier() {
        return supplier;
    }

    public void setSupplier(Supplier supplier) {
        this.supplier = supplier;
    }

    public String getPlate() {
        return plate;
    }

    public void setPlate(String plate) {
        this.plate = plate;
    }

    public String getTrailerPlate() {
        return trailerPlate;
    }

    public void setTrailerPlate(String trailerPlate) {
        this.trailerPlate = trailerPlate;
    }

    public String getOrigin() {
        return origin;
    }

    public void setOrigin(String origin) {
        this.origin = origin;
    }

    public String getDeliveryType() {
        return deliveryType;
    }

    public void setDeliveryType(String deliveryType) {
        this.deliveryType = deliveryType;
    }

    public BigDecimal getAmount() {
        return amount;
    }

    public void setAmount(BigDecimal amount) {
        this.amount = amount;
    }

    public LocalDate getDeliveryDate() {
        return deliveryDate;
    }

    public void setDeliveryDate(LocalDate deliveryDate) {
        this.deliveryDate = deliveryDate;
    }

    public LocalDate getShippingDate() {
        return shippingDate;
    }

    public void setShippingDate(LocalDate shippingDate) {
        this.shippingDate = shippingDate;
    }

    public String getCartaPorteXml() {
        return cartaPorteXml;
    }

    public void setCartaPorteXml(String cartaPorteXml) {
        this.cartaPorteXml = cartaPorteXml;
    }

    public String getCartaPorteCsv() {
        return cartaPorteCsv;
    }

    public void setCartaPorteCsv(String cartaPorteCsv) {
        this.cartaPorteCsv = cartaPorteCsv;
    }

    public String getComments() {
        return comments;
    }

    public void setComments(String comments) {
        this.comments = comments;
    }

    public ShippingStatus getStatus() {
        return status;
    }

    public void setStatus(ShippingStatus status) {
        this.status = status;
    }

    public Long getUserCreatedId() {
        return userCreatedId;
    }

    public void setUserCreatedId(Long userCreatedId) {
        this.userCreatedId = userCreatedId;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }
}

package com.sodimac.aclaraciones.api.model.entity;

import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Table(name = "SHIPPINGGUIDE_HIST")
public class ShippingGuideHist {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "IDHIST")
    private Long idHist;

    @Column(name = "IDSHIPPINGGUIDE")
    private Long idShippingGuide;

    @Column(name = "IDPURCHASEORDER")
    private Long purchaseOrderId;

    @Column(name = "SHIPPINGGUIDE")
    private String guideNumber;

    @Column(name = "SUPPLIERNUMBER")
    private Long supplierNumber;

    @Column(name = "LICENSEPLATE")
    private String plate;

    @Column(name = "TRAILERPLATE")
    private String trailerPlate;

    @Column(name = "ORIGIN")
    private String origin;

    @Column(name = "DELIVERYTYPE")
    private String deliveryType;

    @Column(name = "TOTALAMOUNT", precision = 18, scale = 2)
    private BigDecimal amount;

    @Column(name = "DELIVERYDATE")
    private LocalDate deliveryDate;

    @Column(name = "SENTDATE")
    private LocalDate shippingDate;

    @Lob
    @Column(name = "CARTAPORTEXML")
    private String cartaPorteXml;

    @Lob
    @Column(name = "CARTAPORTECSV")
    private String cartaPorteCsv;

    @Column(name = "COMMENTS", length = 2000)
    private String comments;

    @Column(name = "STATUS")
    private Integer statusId;

    @Column(name = "IDUSERCREATED")
    private Long userCreatedId;

    @Column(name = "CREATEDAT")
    private LocalDateTime createdAt;

    @Column(name = "UPDATEDAT")
    private LocalDateTime updatedAt;

    @Column(name = "ARCHIVED_AT")
    private LocalDateTime archivedAt;

    @Column(name = "ARCHIVED_BY", length = 64)
    private String archivedBy;

    // Getters & Setters
    public Long getIdHist() {
        return idHist;
    }

    public void setIdHist(Long idHist) {
        this.idHist = idHist;
    }

    public Long getIdShippingGuide() {
        return idShippingGuide;
    }

    public void setIdShippingGuide(Long idShippingGuide) {
        this.idShippingGuide = idShippingGuide;
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

    public Integer getStatusId() {
        return statusId;
    }

    public void setStatusId(Integer statusId) {
        this.statusId = statusId;
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

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(LocalDateTime updatedAt) {
        this.updatedAt = updatedAt;
    }

    public LocalDateTime getArchivedAt() {
        return archivedAt;
    }

    public void setArchivedAt(LocalDateTime archivedAt) {
        this.archivedAt = archivedAt;
    }

    public String getArchivedBy() {
        return archivedBy;
    }

    public void setArchivedBy(String archivedBy) {
        this.archivedBy = archivedBy;
    }
}

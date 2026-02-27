package com.sodimac.aclaraciones.api.model.entity;

import java.time.LocalDateTime;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;
import jakarta.persistence.*;

/**
 * Tabla: public.related_information
 */
@Entity
@Table(name = "related_information")
public class RelatedInformation {

    /* ---------- PK ---------- */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /* ---------- columnas de datos ---------- */
    @Column(length = 64, nullable = false)
    private String title;

    @Column(length = 256, nullable = false)
    private String link;

    /** Imagen almacenada como BYTEA (nullable) */
    @Column(name = "image_data")
    private byte[] imageData;

    /** Nombre del archivo (nullable) */
    @Column(name = "image_path", length = 255)
    private String imagePath;

    /** Activo: true por defecto */
    @Column(name = "is_active", nullable = false, columnDefinition = "boolean default true")
    private Boolean isActive = Boolean.TRUE;

    /* ---------- nuevos campos ---------- */
    @Column(name = "business_unit_id")
    private Integer businessUnitId;

    @Column(name = "country_id")
    private Integer countryId;

    /* ---------- timestamps ---------- */
    @CreationTimestamp
    @Column(name = "created_at", updatable = false, nullable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;

    /* ---------- getters & setters ---------- */
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String t) {
        this.title = t;
    }

    public String getLink() {
        return link;
    }

    public void setLink(String l) {
        this.link = l;
    }

    public byte[] getImageData() {
        return imageData;
    }

    public void setImageData(byte[] d) {
        this.imageData = d;
    }

    public String getImagePath() {
        return imagePath;
    }

    public void setImagePath(String p) {
        this.imagePath = p;
    }

    public Boolean getIsActive() {
        return isActive;
    }

    public void setIsActive(Boolean a) {
        this.isActive = a;
    }

    public Integer getBusinessUnitId() {
        return businessUnitId;
    }

    public void setBusinessUnitId(Integer businessUnitId) {
        this.businessUnitId = businessUnitId;
    }

    public Integer getCountryId() {
        return countryId;
    }

    public void setCountryId(Integer countryId) {
        this.countryId = countryId;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }
}

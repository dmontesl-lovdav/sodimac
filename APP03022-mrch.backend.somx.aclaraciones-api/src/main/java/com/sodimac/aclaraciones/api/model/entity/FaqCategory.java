/*────────────────────────────────────────────────────────────
 * src/main/java/com/sodimac/aclaraciones/api/model/entity/FaqCategory.java
 *────────────────────────────────────────────────────────────*/
package com.sodimac.aclaraciones.api.model.entity;

import jakarta.persistence.*;
import java.time.OffsetDateTime;

// Hibernate 6+
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

// Si usas Hibernate 5.x, descomenta estas dos líneas:
// import org.hibernate.annotations.Type;
// import org.hibernate.type.BinaryType;

@Entity
@Table(name = "faq_category")
public class FaqCategory {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "name", nullable = false, length = 64)
    private String name;

    @Column(name = "description", nullable = false, length = 256)
    private String description;

    @Column(name = "icon_path")
    private String iconPath;

    /**
     * Imagen almacenada como BYTEA.
     * - @JdbcTypeCode(SqlTypes.BINARY) fuerza a Hibernate a bindear binario (no
     * bigint).
     * - columnDefinition="bytea" explicita el tipo en Postgres.
     * - Mantengo LAZY como lo tenías.
     */
    @Lob
    @Basic(fetch = FetchType.LAZY)
    @JdbcTypeCode(SqlTypes.BINARY) // Hibernate 6+
    // @Type(type = "org.hibernate.type.BinaryType") // Hibernate 5.x → descomentar
    // si aplica
    @Column(name = "image_data", columnDefinition = "bytea")
    private byte[] imageData;

    @Column(name = "image_name")
    private String imageName;

    @Column(name = "is_active", nullable = false)
    private Boolean isActive = Boolean.TRUE;

    @Column(name = "created_at", nullable = false, updatable = false)
    private OffsetDateTime createdAt;

    @Column(name = "updated_at", nullable = false)
    private OffsetDateTime updatedAt;

    /* getters & setters */

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

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

    public String getIconPath() {
        return iconPath;
    }

    public void setIconPath(String iconPath) {
        this.iconPath = iconPath;
    }

    public byte[] getImageData() {
        return imageData;
    }

    public void setImageData(byte[] imageData) {
        this.imageData = imageData;
    }

    public String getImageName() {
        return imageName;
    }

    public void setImageName(String imageName) {
        this.imageName = imageName;
    }

    public Boolean getIsActive() {
        return isActive;
    }

    public void setIsActive(Boolean isActive) {
        this.isActive = isActive;
    }

    public OffsetDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(OffsetDateTime createdAt) {
        this.createdAt = createdAt;
    }

    public OffsetDateTime getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(OffsetDateTime updatedAt) {
        this.updatedAt = updatedAt;
    }

    @PrePersist
    public void prePersist() {
        OffsetDateTime now = OffsetDateTime.now();
        createdAt = now;
        updatedAt = now;
        if (isActive == null)
            isActive = Boolean.TRUE;
    }

    @PreUpdate
    public void preUpdate() {
        updatedAt = OffsetDateTime.now();
        if (isActive == null)
            isActive = Boolean.TRUE;
    }
}

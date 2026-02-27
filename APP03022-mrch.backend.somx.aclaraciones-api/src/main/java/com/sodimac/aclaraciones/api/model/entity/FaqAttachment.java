/*---------------------------------------------------------------------------
 * src/main/java/com/sodimac/aclaraciones/api/model/entity/FaqAttachment.java
 *--------------------------------------------------------------------------*/
package com.sodimac.aclaraciones.api.model.entity;

import jakarta.persistence.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;

/**
 * Archivos (imagen, vídeo, PDF, etc.) asociados a una FAQ.
 *
 * • En esta versión guardamos el binario en la columna {@code data}.
 * • Si en un futuro se migra a S3/GCS basta con poner {@code data = NULL}
 * y rellenar {@code filePath} con la URL firmada/pública.
 */
@Entity
@Table(name = "faq_attachment")
public class FaqAttachment {

    /* ---------- PK ---------- */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /* ---------- relaciones ---------- */
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "faq_id", nullable = false)
    private Faq faq;

    /* ---------- columnas ---------- */
    @Column(name = "file_name", nullable = false, length = 255)
    private String fileName;

    /** MIME type — p.ej. {@code image/jpeg}, {@code application/pdf} */
    @Column(name = "content_type", nullable = false, length = 120)
    private String contentType;

    /** Binario en la BD — puede ser {@code NULL} si se externaliza a S3/GCS */
    @Lob
    @Basic(fetch = FetchType.LAZY)
    @Column(name = "data")
    private byte[] data;

    /** Ruta/URL si el binario vive fuera de la BD (opcional) */
    @Column(name = "file_path", length = 1024)
    private String filePath;

    /**
     * Tamaño calculado en KB (redondeo hacia arriba).
     * Si hay datos y el cálculo da 0, se fuerza a 1.
     */
    @Column(name = "size_kb", nullable = false)
    private Integer sizeKb = 0;

    @Column(name = "is_active", nullable = false)
    private Boolean isActive = Boolean.TRUE;

    @CreationTimestamp
    @Column(name = "created_at", updatable = false, nullable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;

    /* ---------- constructores ---------- */
    protected FaqAttachment() {
        /* JPA */ }

    /**
     * Constructor de conveniencia.
     * Usado por el command service al persistir nuevos adjuntos.
     */
    public FaqAttachment(Faq faq, String fileName, String contentType, byte[] data) {
        this.faq = faq;
        this.fileName = fileName;
        this.contentType = contentType;
        this.setData(data); // calcula sizeKb correctamente
    }

    /* ---------- getters & setters ---------- */
    public Long getId() {
        return id;
    }

    public Faq getFaq() {
        return faq;
    }

    public void setFaq(Faq faq) {
        this.faq = faq;
    }

    public String getFileName() {
        return fileName;
    }

    public void setFileName(String fileName) {
        this.fileName = fileName;
    }

    public String getContentType() {
        return contentType;
    }

    public void setContentType(String contentType) {
        this.contentType = contentType;
    }

    public byte[] getData() {
        return data;
    }

    public void setData(byte[] data) {
        this.data = data;
        int kb = 0;
        if (data != null && data.length > 0) {
            kb = (int) Math.ceil(data.length / 1024.0);
            if (kb == 0)
                kb = 1;
        }
        this.sizeKb = kb;
    }

    public String getFilePath() {
        return filePath;
    }

    public void setFilePath(String filePath) {
        this.filePath = filePath;
    }

    public Integer getSizeKb() {
        return sizeKb;
    }

    public Boolean getIsActive() {
        return isActive;
    }

    public void setIsActive(Boolean active) {
        isActive = active;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }
}

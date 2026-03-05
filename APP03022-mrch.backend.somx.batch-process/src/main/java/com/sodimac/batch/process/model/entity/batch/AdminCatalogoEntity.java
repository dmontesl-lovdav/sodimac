package com.sodimac.batch.process.model.entity.batch;

import javax.persistence.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "adminCatalogo")
@IdClass(AdminCatalogoId.class)
public class AdminCatalogoEntity {

    @Id
    @Column(name = "idCatalogo")
    private Integer idCatalogo;

    @Id
    @Column(name = "idElemento")
    private Integer idElemento;

    @Column(name = "descripcion", length = 50)
    private String descripcion;

    @Column(name = "estatus", nullable = false)
    private Integer estatus;

    @Column(name = "usuarioCreacion")
    private Integer usuarioCreacion;

    @Column(name = "fechaCreacion", nullable = false)
    private LocalDateTime fechaCreacion;

    @Column(name = "usuarioActualizacion")
    private Integer usuarioActualizacion;

    @Column(name = "fechaActualizacion")
    private LocalDateTime fechaActualizacion;

    @Column(name = "elementoConversion", length = 50)
    private String elementoConversion;

    public AdminCatalogoEntity() {}

    public Integer getIdCatalogo() { return idCatalogo; }
    public void setIdCatalogo(Integer idCatalogo) { this.idCatalogo = idCatalogo; }

    public Integer getIdElemento() { return idElemento; }
    public void setIdElemento(Integer idElemento) { this.idElemento = idElemento; }

    public String getDescripcion() { return descripcion; }
    public void setDescripcion(String descripcion) { this.descripcion = descripcion; }

    public Integer getEstatus() { return estatus; }
    public void setEstatus(Integer estatus) { this.estatus = estatus; }

    public Integer getUsuarioCreacion() { return usuarioCreacion; }
    public void setUsuarioCreacion(Integer usuarioCreacion) { this.usuarioCreacion = usuarioCreacion; }

    public LocalDateTime getFechaCreacion() { return fechaCreacion; }
    public void setFechaCreacion(LocalDateTime fechaCreacion) { this.fechaCreacion = fechaCreacion; }

    public Integer getUsuarioActualizacion() { return usuarioActualizacion; }
    public void setUsuarioActualizacion(Integer usuarioActualizacion) { this.usuarioActualizacion = usuarioActualizacion; }

    public LocalDateTime getFechaActualizacion() { return fechaActualizacion; }
    public void setFechaActualizacion(LocalDateTime fechaActualizacion) { this.fechaActualizacion = fechaActualizacion; }

    public String getElementoConversion() { return elementoConversion; }
    public void setElementoConversion(String elementoConversion) { this.elementoConversion = elementoConversion; }
}

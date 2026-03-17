package com.sodimac.batch.fiscal.download.model.dto;

import java.math.BigDecimal;

public class StatusUpdateRequestDto {
    private BigDecimal numeroProveedor;
    private Integer estatusOrigen;
    private Integer estatusDestino;
    private Long idUsuarioActualizacion;
    private String comentario;

    public StatusUpdateRequestDto() {}

    public BigDecimal getNumeroProveedor() { return numeroProveedor; }
    public void setNumeroProveedor(BigDecimal numeroProveedor) { this.numeroProveedor = numeroProveedor; }

    public Integer getEstatusOrigen() { return estatusOrigen; }
    public void setEstatusOrigen(Integer estatusOrigen) { this.estatusOrigen = estatusOrigen; }

    public Integer getEstatusDestino() { return estatusDestino; }
    public void setEstatusDestino(Integer estatusDestino) { this.estatusDestino = estatusDestino; }

    public Long getIdUsuarioActualizacion() { return idUsuarioActualizacion; }
    public void setIdUsuarioActualizacion(Long idUsuarioActualizacion) { this.idUsuarioActualizacion = idUsuarioActualizacion; }

    public String getComentario() { return comentario; }
    public void setComentario(String comentario) { this.comentario = comentario; }
}

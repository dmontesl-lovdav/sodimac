package com.sodimac.batch.process.model.dto;

import java.time.LocalDate;

public class InvoiceSearchRequestDto {
    private LocalDate fechaInicioRecepcion;
    private LocalDate fechaFinalRecepcion;
    private String tipoDocumento;
    private Integer estatus;
    private Integer page;
    private Integer size;
    private String sortBy;
    private String sortDirection;

    public InvoiceSearchRequestDto() {}

    public LocalDate getFechaInicioRecepcion() { return fechaInicioRecepcion; }
    public void setFechaInicioRecepcion(LocalDate fechaInicioRecepcion) { this.fechaInicioRecepcion = fechaInicioRecepcion; }

    public LocalDate getFechaFinalRecepcion() { return fechaFinalRecepcion; }
    public void setFechaFinalRecepcion(LocalDate fechaFinalRecepcion) { this.fechaFinalRecepcion = fechaFinalRecepcion; }

    public String getTipoDocumento() { return tipoDocumento; }
    public void setTipoDocumento(String tipoDocumento) { this.tipoDocumento = tipoDocumento; }

    public Integer getEstatus() { return estatus; }
    public void setEstatus(Integer estatus) { this.estatus = estatus; }

    public Integer getPage() { return page; }
    public void setPage(Integer page) { this.page = page; }

    public Integer getSize() { return size; }
    public void setSize(Integer size) { this.size = size; }

    public String getSortBy() { return sortBy; }
    public void setSortBy(String sortBy) { this.sortBy = sortBy; }

    public String getSortDirection() { return sortDirection; }
    public void setSortDirection(String sortDirection) { this.sortDirection = sortDirection; }
}

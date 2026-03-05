package com.sodimac.batch.process.mapper;

import com.sodimac.batch.process.model.dto.InvoiceSearchRequestDto;

import java.time.LocalDate;

public class InvoiceSearchRequestMapper {

    public static InvoiceSearchRequestDto toDto(Integer estatus, String tipoDocumento,
                                                 LocalDate dateFrom, LocalDate dateTo,
                                                 int page, int size) {
        InvoiceSearchRequestDto dto = new InvoiceSearchRequestDto();
        dto.setEstatus(estatus);
        dto.setTipoDocumento(tipoDocumento);
        dto.setFechaInicioRecepcion(dateFrom);
        dto.setFechaFinalRecepcion(dateTo);
        dto.setPage(page);
        dto.setSize(size);
        dto.setSortBy("createdAt");
        dto.setSortDirection("ASC");
        return dto;
    }
}

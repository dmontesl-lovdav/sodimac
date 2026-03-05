package com.sodimac.batch.process.mapper;

import com.sodimac.batch.process.model.dto.StatusUpdateRequestDto;

import java.math.BigDecimal;

public class StatusUpdateRequestMapper {

    public static StatusUpdateRequestDto toDto(BigDecimal numeroProveedor, Integer estatusOrigen,
                                                Integer estatusDestino, String comentario) {
        StatusUpdateRequestDto dto = new StatusUpdateRequestDto();
        dto.setNumeroProveedor(numeroProveedor);
        dto.setEstatusOrigen(estatusOrigen);
        dto.setEstatusDestino(estatusDestino);
        dto.setIdUsuarioActualizacion(0L);
        dto.setComentario(comentario);
        return dto;
    }
}

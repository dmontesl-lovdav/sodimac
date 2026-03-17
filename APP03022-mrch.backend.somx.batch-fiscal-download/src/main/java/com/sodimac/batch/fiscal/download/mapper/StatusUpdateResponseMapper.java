package com.sodimac.batch.fiscal.download.mapper;

import com.sodimac.batch.fiscal.download.model.dto.StatusUpdateResponseDto;

public class StatusUpdateResponseMapper {

    public static StatusUpdateResponseDto toError(String message) {
        StatusUpdateResponseDto dto = new StatusUpdateResponseDto();
        dto.setSuccess(false);
        dto.setMessage(message);
        return dto;
    }
}

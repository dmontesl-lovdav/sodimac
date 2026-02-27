package com.sodimac.fiscal.api.mapper;

import com.sodimac.fiscal.api.model.dto.LogDto;
import com.sodimac.fiscal.api.model.entity.LogEntity;
import com.sodimac.fiscal.api.service.ToolsService;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.stream.Collectors;

@Component
public class LogMapper {

    public LogDto toDto(LogEntity entity) {
        if (entity == null) {
            return null;
        }

        LogDto dto = new LogDto();
        dto.setLogId(entity.getLogId());
        dto.setPacId(entity.getPacId());
        dto.setVersionId(entity.getVersionId());
        dto.setCfdiUuid(entity.getCfdiUuid());
        dto.setOperationType(entity.getOperationType());
        dto.setTransactionDate(entity.getTransactionDate());
        dto.setRecordStartDate(entity.getRecordStartDate());
        dto.setRecordEndDate(entity.getRecordEndDate());
        dto.setStatusCode(entity.getStatusCode());
        dto.setStatusMessage(entity.getStatusMessage());
        dto.setRequestData(entity.getRequestData());
        dto.setResponseData(entity.getResponseData());

        return dto;
    }

    public LogEntity toEntity(LogDto dto) {
        if (dto == null) {
            return null;
        }

        LogEntity entity = new LogEntity();
        entity.setLogId(dto.getLogId());
        entity.setPacId(dto.getPacId());
        entity.setVersionId(dto.getVersionId());
        entity.setCfdiUuid(dto.getCfdiUuid());
        entity.setOperationType(dto.getOperationType());
        entity.setTransactionDate(dto.getTransactionDate());
        entity.setRecordStartDate(dto.getRecordStartDate());
        entity.setRecordEndDate(dto.getRecordEndDate());
        entity.setStatusCode(dto.getStatusCode());
        entity.setStatusMessage(dto.getStatusMessage());
        entity.setRequestData(dto.getRequestData());
        entity.setResponseData(dto.getResponseData());

        return entity;
    }

    public List<LogDto> toDtoList(List<LogEntity> entities) {
        if (entities == null) {
            return null;
        }

        return entities.stream()
                .map(this::toDto)
                .collect(Collectors.toList());
    }

    public List<LogEntity> toEntityList(List<LogDto> dtos) {
        if (dtos == null) {
            return null;
        }

        return dtos.stream()
                .map(this::toEntity)
                .collect(Collectors.toList());
    }
}
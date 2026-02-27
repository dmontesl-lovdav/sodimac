package com.sodimac.catman.api.mapper;

import com.sodimac.catman.api.model.dto.StatusTrainCreateDto;
import com.sodimac.catman.api.model.dto.StatusTrainDto;
import com.sodimac.catman.api.model.entity.StatusTrain;

/**
 * Mapper para convertir entre entidad StatusTrain y sus DTOs.
 *
 * @author Sodimac Tech Team
 * @since STM-1166
 */
public class StatusTrainMapper {

    private StatusTrainMapper() {
        // Utility class
    }

    /**
     * Convierte una entidad StatusTrain a DTO.
     *
     * @param entity Entidad a convertir
     * @return DTO con los datos de la entidad
     */
    public static StatusTrainDto toDto(StatusTrain entity) {
        if (entity == null) {
            return null;
        }

        return StatusTrainDto.builder()
                .id(entity.getId())
                .optionId(entity.getOptionId())
                .sourceStatus(entity.getSourceStatus())
                .targetStatus(entity.getTargetStatus())
                .createdBy(entity.getCreatedBy())
                .createdAt(entity.getCreatedAt())
                .updatedBy(entity.getUpdatedBy())
                .updatedAt(entity.getUpdatedAt())
                .build();
    }

    /**
     * Convierte un DTO de creación a entidad StatusTrain.
     *
     * @param dto DTO con los datos para crear
     * @return Entidad lista para persistir
     */
    public static StatusTrain toEntity(StatusTrainCreateDto dto) {
        if (dto == null) {
            return null;
        }

        return StatusTrain.builder()
                .optionId(dto.getOptionId())
                .sourceStatus(dto.getSourceStatus())
                .targetStatus(dto.getTargetStatus())
                .createdBy(dto.getCreatedBy())
                .build();
    }
}

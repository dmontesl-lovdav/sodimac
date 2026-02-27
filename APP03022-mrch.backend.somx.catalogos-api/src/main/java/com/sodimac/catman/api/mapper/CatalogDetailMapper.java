package com.sodimac.catman.api.mapper;

import java.util.List;
import java.util.Map;

import org.springframework.stereotype.Component;

import com.sodimac.catman.api.model.dto.CatalogDetailDto;
import com.sodimac.catman.api.model.entity.CatalogDetail;

@Component
public class CatalogDetailMapper {

    public CatalogDetailDto toDto(CatalogDetail entity, Map<Integer, String> dictionary) {
        if (entity == null) {
            return null;
        }

        return CatalogDetailDto.builder()
                .key(entity.getKey())
                .internalStatus(entity.getInternalStatus())
                .externalKey(entity.getExternalKey())
                .value(entity.getValue())
                .description(dictionary.getOrDefault(entity.getDictId(), ""))
                .color(entity.getColor())
                .sortOrder(entity.getSortOrder())
                .validFrom(entity.getValidFrom())
                .validTo(entity.getValidTo())
                .attributes(entity.getAttributes())
                .build();
    }

    public CatalogDetail toEntity(CatalogDetailDto dto, Integer dictId) {
        if (dto == null) {
            return null;
        }

        return CatalogDetail.builder()
                .key(dto.getKey())
                .externalKey(dto.getExternalKey())
                .value(dto.getValue())
                .dictId(dictId)
                .color(dto.getColor())
                .sortOrder(dto.getSortOrder())
                .validFrom(dto.getValidFrom())
                .validTo(dto.getValidTo())
                .attributes(dto.getAttributes())
                .build();
    }

    public List<CatalogDetailDto> toDtoList(List<CatalogDetail> entities, Map<Integer, String> dictionary) {
        if (entities == null) {
            return null;
        }

        return entities.stream()
                .map(e -> toDto(e, dictionary))
                .toList();
    }
}

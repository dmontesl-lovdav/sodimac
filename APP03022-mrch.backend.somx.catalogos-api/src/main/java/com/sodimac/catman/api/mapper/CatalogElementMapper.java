package com.sodimac.catman.api.mapper;

import java.util.List;
import java.util.stream.Collectors;

import org.springframework.stereotype.Component;

import com.sodimac.catman.api.model.dto.CatalogElementDto;
import com.sodimac.catman.api.model.dto.CatalogSimpleDto;
import com.sodimac.catman.api.model.entity.CatalogDetail;
import com.sodimac.catman.api.model.entity.CatalogHeader;
import com.sodimac.catman.api.repository.CatalogDetailRepository;
import com.sodimac.catman.api.repository.CatalogHeaderRepository;

@Component
public class CatalogElementMapper {

    private final CatalogHeaderRepository headerRepository;
    private final CatalogDetailRepository detailRepository;

    public CatalogElementMapper(CatalogHeaderRepository headerRepository, CatalogDetailRepository detailRepository) {
        this.headerRepository = headerRepository;
        this.detailRepository = detailRepository;
    }

    public CatalogElementDto toDto(CatalogDetail entity) {
        if (entity == null) {
            return null;
        }

        CatalogElementDto dto = CatalogElementDto.builder()
                .id(entity.getId())
                .catalogId(entity.getHeader() != null ? entity.getHeader().getId() : null)
                .catalogCode(entity.getHeader() != null ? entity.getHeader().getCode() : null)
                .element(entity.getKey())
                .value(entity.getValue())
                .key(entity.getKey())
                .validFrom(entity.getValidFrom())
                .validTo(entity.getValidTo())
                .status(entity.getStatus())
                .statusDescription(entity.getStatus() == 1 ? "Activo" : "Inactivo")
                .parentCatalogId(entity.getParentCatalogId())
                .parentElementId(entity.getParentElementId())
                .createdBy(entity.getCreatedBy())
                .createdAt(entity.getCreatedAt())
                .updatedBy(entity.getUpdatedBy())
                .updatedAt(entity.getUpdatedAt())
                .sortOrder(entity.getSortOrder())
                .attributes(entity.getAttributes())
                .build();

        if (entity.getParentCatalogId() != null) {
            headerRepository.findById(entity.getParentCatalogId())
                    .ifPresent(parent -> dto.setParentCatalogName(parent.getName()));
        }

        if (entity.getParentElementId() != null) {
            detailRepository.findById(entity.getParentElementId())
                    .ifPresent(parent -> dto.setParentElementName(parent.getKey()));
        }

        return dto;
    }

    public List<CatalogElementDto> toDtoList(List<CatalogDetail> entities) {
        return entities.stream()
                .map(this::toDto)
                .collect(Collectors.toList());
    }

    public CatalogSimpleDto toSimpleDto(CatalogHeader entity) {
        if (entity == null) {
            return null;
        }

        return CatalogSimpleDto.builder()
                .id(entity.getId())
                .code(entity.getCode())
                .name(entity.getName())
                .description(entity.getDescription())
                .catalogType(entity.getCatalogType())
                .status(entity.getStatus())
                .build();
    }

    public List<CatalogSimpleDto> toSimpleDtoList(List<CatalogHeader> entities) {
        return entities.stream()
                .map(this::toSimpleDto)
                .collect(Collectors.toList());
    }
}








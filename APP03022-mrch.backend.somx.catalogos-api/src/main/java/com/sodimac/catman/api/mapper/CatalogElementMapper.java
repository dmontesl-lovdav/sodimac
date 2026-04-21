package com.sodimac.catman.api.mapper;

import java.util.List;
import java.util.stream.Collectors;

import org.springframework.stereotype.Component;

import com.sodimac.catman.api.model.dto.CatalogElementDto;
import com.sodimac.catman.api.model.dto.CatalogSimpleDto;
import com.sodimac.catman.api.model.entity.CatalogDetail;
import com.sodimac.catman.api.model.entity.CatalogHeader;
import com.sodimac.catman.api.model.entity.DictionaryLang;
import com.sodimac.catman.api.repository.CatalogDetailRepository;
import com.sodimac.catman.api.repository.CatalogHeaderRepository;
import com.sodimac.catman.api.repository.DictionaryLangRepository;

@Component
public class CatalogElementMapper {

    private static final int DEFAULT_LANG_ID = 1;

    private final CatalogHeaderRepository headerRepository;
    private final CatalogDetailRepository detailRepository;
    private final DictionaryLangRepository dictionaryLangRepository;

    public CatalogElementMapper(CatalogHeaderRepository headerRepository,
                                CatalogDetailRepository detailRepository,
                                DictionaryLangRepository dictionaryLangRepository) {
        this.headerRepository = headerRepository;
        this.detailRepository = detailRepository;
        this.dictionaryLangRepository = dictionaryLangRepository;
    }

    public CatalogElementDto toDto(CatalogDetail entity) {
        if (entity == null) {
            return null;
        }

        String elementName = entity.getKey();
        if (entity.getDictId() != null && entity.getDictId() > 0) {
            elementName = dictionaryLangRepository.findByDictIdAndLangId(entity.getDictId(), DEFAULT_LANG_ID)
                    .map(DictionaryLang::getDescription)
                    .orElse(entity.getKey());
        }

        CatalogElementDto dto = CatalogElementDto.builder()
                .id(entity.getId())
                .catalogId(entity.getHeader() != null ? entity.getHeader().getId() : null)
                .catalogCode(entity.getHeader() != null ? entity.getHeader().getCode() : null)
                .element(elementName)
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
                .externalKey(entity.getExternalKey())
                .sortOrder(entity.getSortOrder())
                .attributes(entity.getAttributes())
                .build();

        if (entity.getParentCatalogId() != null) {
            headerRepository.findById(entity.getParentCatalogId())
                    .ifPresent(parent -> dto.setParentCatalogName(parent.getName()));
        }

        if (entity.getParentElementId() != null) {
            detailRepository.findById(entity.getParentElementId())
                    .ifPresent(parent -> {
                        String parentName = parent.getKey();
                        if (parent.getDictId() != null && parent.getDictId() > 0) {
                            parentName = dictionaryLangRepository.findByDictIdAndLangId(parent.getDictId(), DEFAULT_LANG_ID)
                                    .map(DictionaryLang::getDescription)
                                    .orElse(parent.getKey());
                        }
                        dto.setParentElementName(parentName);
                    });
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

package com.sodimac.catman.api.service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.sodimac.catman.api.exception.GenericException;
import com.sodimac.catman.api.mapper.CatalogElementMapper;
import com.sodimac.catman.api.model.dto.CatalogElementCreateDto;
import com.sodimac.catman.api.model.dto.CatalogElementDto;
import com.sodimac.catman.api.model.dto.CatalogElementPageResponse;
import com.sodimac.catman.api.model.dto.CatalogElementUpdateDto;
import com.sodimac.catman.api.model.dto.CatalogSimpleDto;
import com.sodimac.catman.api.model.entity.CatalogDetail;
import com.sodimac.catman.api.model.entity.CatalogHeader;
import com.sodimac.catman.api.model.entity.DictionaryLang;
import com.sodimac.catman.api.repository.CatalogDetailRepository;
import com.sodimac.catman.api.repository.CatalogHeaderRepository;
import com.sodimac.catman.api.repository.DictionaryLangRepository;
import com.sodimac.catman.api.repository.specification.CatalogElementSpecification;

@Service
public class CatalogElementServiceImpl implements CatalogElementService {

    private static final String CATALOG_TYPE_PRIMARIO = "PRIMARIO";
    private static final String CATALOG_TYPE_SECUNDARIO = "SECUNDARIO";
    private static final int DEFAULT_LANG_ID = 1;

    private final CatalogHeaderRepository headerRepository;
    private final CatalogDetailRepository detailRepository;
    private final DictionaryLangRepository dictionaryLangRepository;
    private final CatalogElementMapper elementMapper;

    public CatalogElementServiceImpl(
            CatalogHeaderRepository headerRepository,
            CatalogDetailRepository detailRepository,
            DictionaryLangRepository dictionaryLangRepository,
            CatalogElementMapper elementMapper) {
        this.headerRepository = headerRepository;
        this.detailRepository = detailRepository;
        this.dictionaryLangRepository = dictionaryLangRepository;
        this.elementMapper = elementMapper;
    }

    @Override
    public CatalogElementPageResponse findElements(
            Integer catalogId,
            Integer elementId,
            String element,
            String value,
            Integer parentCatalogId,
            Integer parentElementId,
            Integer status,
            String key,
            Pageable pageable) {

        headerRepository.findById(catalogId)
                .orElseThrow(() -> new GenericException(404, "Catálogo no encontrado con ID: " + catalogId));

        var spec = CatalogElementSpecification.withFilters(
                catalogId, elementId, element, value, parentCatalogId, parentElementId, status, key);

        Page<CatalogDetail> page = detailRepository.findAll(spec, pageable);

        List<CatalogElementDto> items = elementMapper.toDtoList(page.getContent());

        return CatalogElementPageResponse.builder()
                .items(items)
                .page(page.getNumber() + 1)
                .pageSize(page.getSize())
                .total(page.getTotalElements())
                .totalPages(page.getTotalPages())
                .hasNext(page.hasNext())
                .hasPrevious(page.hasPrevious())
                .build();
    }

    @Override
    public Optional<CatalogElementDto> findElementById(Integer catalogId, Integer elementId) {
        return detailRepository.findById(elementId)
                .filter(d -> d.getHeader().getId().equals(catalogId))
                .map(elementMapper::toDto);
    }

    @Override
    @Transactional
    public CatalogElementDto createElement(Integer catalogId, CatalogElementCreateDto createDto, String userId) {
        CatalogHeader catalog = headerRepository.findById(catalogId)
                .orElseThrow(() -> new GenericException(404, "Catálogo no encontrado con ID: " + catalogId));

        checkDuplicateElementName(catalogId, createDto.getElement(), null);

        validateDates(createDto.getValidFrom(), createDto.getValidTo());

        Integer parentCatId = createDto.getParentCatalogId();
        Integer parentElemId = createDto.getParentElementId();
        if (parentElemId != null && parentCatId == null) {
            detailRepository.findById(parentElemId).ifPresent(parent -> {
                if (parent.getHeader() != null) {
                    createDto.setParentCatalogId(parent.getHeader().getId());
                }
            });
            parentCatId = createDto.getParentCatalogId();
        }

        validateParentRelation(catalog, parentCatId, parentElemId);

        String generatedKey = generateNextKey(catalog);

        int dictId = createDictionaryEntry(createDto.getElement());

        CatalogDetail detail = CatalogDetail.builder()
                .header(catalog)
                .key(generatedKey)
                .value(createDto.getValue())
                .validFrom(createDto.getValidFrom())
                .validTo(createDto.getValidTo())
                .parentCatalogId(parentCatId)
                .parentElementId(parentElemId)
                .externalKey(createDto.getExternalKey() != null && !createDto.getExternalKey().isBlank() ? createDto.getExternalKey() : null)
                .sortOrder(createDto.getSortOrder() != null ? createDto.getSortOrder() : 0)
                .attributes(createDto.getAttributes())
                .status(CatalogDetail.STATUS_ACTIVE)
                .dictId(dictId)
                .createdBy(userId)
                .build();

        CatalogDetail saved = detailRepository.save(detail);

        if (catalog.getStatus() != null && catalog.getStatus() == 0) {
            catalog.setStatus(1);
            headerRepository.save(catalog);
        }

        return elementMapper.toDto(saved);
    }

    @Override
    @Transactional
    public CatalogElementDto updateElement(Integer catalogId, Integer elementId, CatalogElementUpdateDto updateDto, String userId) {
        CatalogHeader catalog = headerRepository.findById(catalogId)
                .orElseThrow(() -> new GenericException(404, "Catálogo no encontrado con ID: " + catalogId));

        CatalogDetail detail = detailRepository.findById(elementId)
                .filter(d -> d.getHeader().getId().equals(catalogId))
                .orElseThrow(() -> new GenericException(404, "Elemento no encontrado con ID: " + elementId));

        if (updateDto.getElement() != null && !updateDto.getElement().isBlank()) {
            checkDuplicateElementName(catalogId, updateDto.getElement(), elementId);
            updateDictionaryEntry(detail.getDictId(), updateDto.getElement());
        }

        if (updateDto.getValue() != null) {
            detail.setValue(updateDto.getValue());
        }

        if (updateDto.getValidFrom() != null) {
            detail.setValidFrom(updateDto.getValidFrom());
        }

        if (updateDto.getValidTo() != null) {
            detail.setValidTo(updateDto.getValidTo());
        }

        validateDates(detail.getValidFrom(), detail.getValidTo());

        Integer parentCatId = updateDto.getParentCatalogId();
        Integer parentElemId = updateDto.getParentElementId();
        boolean parentChanged = (parentCatId != null && !parentCatId.equals(detail.getParentCatalogId()))
                || (parentElemId != null && !parentElemId.equals(detail.getParentElementId()))
                || (parentCatId == null && detail.getParentCatalogId() != null)
                || (parentElemId == null && detail.getParentElementId() != null);

        if (parentChanged) {
            if (parentCatId != null || parentElemId != null) {
                validateParentRelation(catalog, parentCatId, parentElemId);
            }
            detail.setParentCatalogId(parentCatId);
            detail.setParentElementId(parentElemId);
        }

        if (updateDto.getSortOrder() != null) {
            detail.setSortOrder(updateDto.getSortOrder());
        }

        if (updateDto.getAttributes() != null) {
            detail.setAttributes(updateDto.getAttributes());
        }

        if (updateDto.getExternalKey() != null) {
            detail.setExternalKey(updateDto.getExternalKey().isBlank() ? null : updateDto.getExternalKey());
        }

        if (updateDto.getStatus() != null) {
            detail.setStatus(updateDto.getStatus());
        }

        detail.setUpdatedBy(userId);
        detail.setUpdatedAt(LocalDateTime.now());

        CatalogDetail saved = detailRepository.save(detail);
        return elementMapper.toDto(saved);
    }

    @Override
    @Transactional
    public CatalogElementDto changeStatus(Integer elementId, Integer newStatus, String userId) {
        CatalogDetail detail = detailRepository.findById(elementId)
                .orElseThrow(() -> new GenericException(404, "Elemento no encontrado con ID: " + elementId));

        if (newStatus != 0 && newStatus != 1) {
            throw new GenericException(400, "Estatus inválido. Use 0 (Inactivo) o 1 (Activo).");
        }

        detail.setStatus(newStatus);
        detail.setUpdatedBy(userId);
        detail.setUpdatedAt(LocalDateTime.now());

        CatalogDetail saved = detailRepository.save(detail);
        return elementMapper.toDto(saved);
    }

    @Override
    public List<CatalogSimpleDto> findPrimaryCatalogs() {
        List<CatalogHeader> primaries = headerRepository.findByCatalogTypeAndStatus(CATALOG_TYPE_PRIMARIO, 1);
        List<CatalogHeader> hierarchicals = headerRepository.findByCatalogTypeAndStatus("HIERARCHICAL", 1);
        primaries.addAll(hierarchicals);
        return elementMapper.toSimpleDtoList(primaries);
    }

    @Override
    public List<CatalogElementDto> findActiveElements(Integer catalogId) {
        List<CatalogDetail> elements = detailRepository.findByHeaderIdAndStatus(catalogId, CatalogDetail.STATUS_ACTIVE);
        return elementMapper.toDtoList(elements);
    }

    @Override
    public Optional<CatalogSimpleDto> findCatalogById(Integer catalogId) {
        return headerRepository.findById(catalogId)
                .map(elementMapper::toSimpleDto);
    }

    private String generateNextKey(CatalogHeader catalog) {
        String prefix = catalog.getPrefix() != null ? catalog.getPrefix() : "EL";
        String maxKey = detailRepository.findMaxKeyByHeaderId(catalog.getId());

        int nextNum = 1;
        if (maxKey != null && maxKey.startsWith(prefix)) {
            try {
                String numPart = maxKey.substring(prefix.length());
                nextNum = Integer.parseInt(numPart) + 1;
            } catch (NumberFormatException e) {
                Long count = detailRepository.countByHeaderId(catalog.getId());
                nextNum = count.intValue() + 1;
            }
        } else if (maxKey != null) {
            Long count = detailRepository.countByHeaderId(catalog.getId());
            nextNum = count.intValue() + 1;
        }

        return prefix + String.format("%04d", nextNum);
    }

    private int createDictionaryEntry(String elementName) {
        DictionaryLang dictEntry = DictionaryLang.builder()
                .dictId(0)
                .langId(DEFAULT_LANG_ID)
                .description(elementName)
                .build();
        DictionaryLang saved = dictionaryLangRepository.save(dictEntry);
        saved.setDictId(saved.getId());
        dictionaryLangRepository.save(saved);
        return saved.getId();
    }

    private void updateDictionaryEntry(Integer dictId, String newName) {
        if (dictId != null && dictId > 0) {
            dictionaryLangRepository.findByDictIdAndLangId(dictId, DEFAULT_LANG_ID)
                    .ifPresent(dict -> {
                        dict.setDescription(newName);
                        dictionaryLangRepository.save(dict);
                    });
        }
    }

    private String getElementNameFromDict(CatalogDetail detail) {
        if (detail.getDictId() != null && detail.getDictId() > 0) {
            return dictionaryLangRepository.findByDictIdAndLangId(detail.getDictId(), DEFAULT_LANG_ID)
                    .map(DictionaryLang::getDescription)
                    .orElse(detail.getKey());
        }
        return detail.getKey();
    }

    private void checkDuplicateElementName(Integer catalogId, String elementName, Integer excludeId) {
        List<CatalogDetail> existing = detailRepository.findByHeaderIdOrderBySortOrder(catalogId);
        for (CatalogDetail el : existing) {
            if (excludeId != null && el.getId().equals(excludeId)) continue;
            String existingName = getElementNameFromDict(el);
            if (existingName != null && existingName.trim().equalsIgnoreCase(elementName.trim())) {
                throw new GenericException(400, "Ya existe un elemento con este nombre en el catálogo.");
            }
        }
    }

    private void validateDates(LocalDate validFrom, LocalDate validTo) {
        if (validFrom == null) {
            return;
        }

        if (validTo != null) {
            if (validTo.isBefore(validFrom)) {
                throw new GenericException(400, "La fecha de fin de vigencia no puede ser anterior a la fecha de inicio.");
            }

            if (!validTo.isAfter(LocalDate.now())) {
                throw new GenericException(400, "La fecha de fin de vigencia debe ser mayor a la fecha actual.");
            }
        }
    }

    private void validateParentRelation(CatalogHeader catalog, Integer parentCatalogId, Integer parentElementId) {
        boolean isPrimary = CATALOG_TYPE_PRIMARIO.equalsIgnoreCase(catalog.getCatalogType())
                || "HIERARCHICAL".equalsIgnoreCase(catalog.getCatalogType());

        if (isPrimary) {
            if (parentCatalogId != null || parentElementId != null) {
                throw new GenericException(400, "Los elementos de catálogos primarios no pueden tener relación padre.");
            }
            return;
        }

        boolean hasCatalog = parentCatalogId != null;
        boolean hasElement = parentElementId != null;
        if (hasCatalog != hasElement) {
            throw new GenericException(400,
                    "Debe proporcionar tanto el catálogo padre como el elemento padre, o dejar ambos vacíos");
        }

        if (parentCatalogId == null) {
            return;
        }

        CatalogDetail parentElement = detailRepository.findById(parentElementId)
                .orElseThrow(() -> new GenericException(400,
                        "El elemento padre seleccionado no existe en el sistema"));

        CatalogHeader parentCatalog = headerRepository.findById(parentCatalogId)
                .orElseThrow(() -> new GenericException(400,
                        "El catálogo padre debe ser de tipo primario o jerárquico"));

        if (!CATALOG_TYPE_PRIMARIO.equalsIgnoreCase(parentCatalog.getCatalogType())
                && !"HIERARCHICAL".equalsIgnoreCase(parentCatalog.getCatalogType())) {
            throw new GenericException(400,
                    "El catálogo padre debe ser de tipo primario o jerárquico");
        }

        if (!parentElement.getHeader().getId().equals(parentCatalogId)) {
            throw new GenericException(400,
                    "El elemento padre no pertenece al catálogo padre seleccionado");
        }
    }
}

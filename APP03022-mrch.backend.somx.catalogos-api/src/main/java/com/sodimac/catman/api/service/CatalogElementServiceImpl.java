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
import com.sodimac.catman.api.repository.CatalogDetailRepository;
import com.sodimac.catman.api.repository.CatalogHeaderRepository;
import com.sodimac.catman.api.repository.specification.CatalogElementSpecification;

@Service
public class CatalogElementServiceImpl implements CatalogElementService {

    private static final String CATALOG_TYPE_PRIMARIO = "PRIMARIO";
    private static final String CATALOG_TYPE_SECUNDARIO = "SECUNDARIO";

    private final CatalogHeaderRepository headerRepository;
    private final CatalogDetailRepository detailRepository;
    private final CatalogElementMapper elementMapper;

    public CatalogElementServiceImpl(
            CatalogHeaderRepository headerRepository,
            CatalogDetailRepository detailRepository,
            CatalogElementMapper elementMapper) {
        this.headerRepository = headerRepository;
        this.detailRepository = detailRepository;
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

        if (detailRepository.existsByHeaderIdAndKeyIgnoreCase(catalogId, createDto.getElement())) {
            throw new GenericException(400, "Ya existe un elemento con este nombre en el catálogo.");
        }

        validateDates(createDto.getValidFrom(), createDto.getValidTo());

        validateParentRelation(catalog, createDto.getParentCatalogId(), createDto.getParentElementId());

        CatalogDetail detail = CatalogDetail.builder()
                .header(catalog)
                .key(createDto.getElement())
                .value(createDto.getValue())
                .validFrom(createDto.getValidFrom())
                .validTo(createDto.getValidTo())
                .parentCatalogId(createDto.getParentCatalogId())
                .parentElementId(createDto.getParentElementId())
                .sortOrder(createDto.getSortOrder() != null ? createDto.getSortOrder() : 0)
                .attributes(createDto.getAttributes())
                .status(CatalogDetail.STATUS_ACTIVE)
                .dictId(0)
                .createdBy(userId)
                .build();

        CatalogDetail saved = detailRepository.save(detail);
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
            if (detailRepository.existsByHeaderIdAndKeyIgnoreCaseAndIdNot(catalogId, updateDto.getElement(), elementId)) {
                throw new GenericException(400, "Ya existe un elemento con este nombre en el catálogo.");
            }
            detail.setKey(updateDto.getElement());
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

        if (updateDto.getParentCatalogId() != null || updateDto.getParentElementId() != null) {
            Integer parentCatId = updateDto.getParentCatalogId() != null ? updateDto.getParentCatalogId() : detail.getParentCatalogId();
            Integer parentElemId = updateDto.getParentElementId() != null ? updateDto.getParentElementId() : detail.getParentElementId();
            validateParentRelation(catalog, parentCatId, parentElemId);
            detail.setParentCatalogId(parentCatId);
            detail.setParentElementId(parentElemId);
        }

        if (updateDto.getSortOrder() != null) {
            detail.setSortOrder(updateDto.getSortOrder());
        }

        if (updateDto.getAttributes() != null) {
            detail.setAttributes(updateDto.getAttributes());
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

        if (newStatus != CatalogDetail.STATUS_ACTIVE && newStatus != CatalogDetail.STATUS_INACTIVE) {
            throw new GenericException(400, "Estatus inválido. Use 1 para Activo o 0 para Inactivo.");
        }

        detail.setStatus(newStatus);
        detail.setUpdatedBy(userId);
        detail.setUpdatedAt(LocalDateTime.now());

        CatalogDetail saved = detailRepository.save(detail);
        return elementMapper.toDto(saved);
    }

    @Override
    public List<CatalogSimpleDto> findPrimaryCatalogs() {
        List<CatalogHeader> catalogs = headerRepository.findByCatalogTypeAndStatus(
                CATALOG_TYPE_PRIMARIO, CatalogHeader.STATUS_ACTIVE);
        return elementMapper.toSimpleDtoList(catalogs);
    }

    @Override
    public List<CatalogElementDto> findActiveElements(Integer catalogId) {
        List<CatalogDetail> elements = detailRepository.findByHeaderIdAndStatus(
                catalogId, CatalogDetail.STATUS_ACTIVE);
        return elementMapper.toDtoList(elements);
    }

    @Override
    public Optional<CatalogSimpleDto> findCatalogById(Integer catalogId) {
        return headerRepository.findById(catalogId)
                .map(elementMapper::toSimpleDto);
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
        boolean isPrimary = CATALOG_TYPE_PRIMARIO.equalsIgnoreCase(catalog.getCatalogType());

        if (isPrimary) {
            if (parentCatalogId != null || parentElementId != null) {
                throw new GenericException(400, "Los elementos de catálogos primarios no pueden tener relación padre.");
            }
        } else {
            if (parentCatalogId != null && parentElementId == null) {
                throw new GenericException(400, "Debe seleccionar un elemento padre cuando se ha elegido un catálogo padre.");
            }

            if (parentCatalogId != null) {
                CatalogHeader parentCatalog = headerRepository.findById(parentCatalogId)
                        .orElseThrow(() -> new GenericException(400, "Catálogo padre no encontrado."));

                if (!CATALOG_TYPE_PRIMARIO.equalsIgnoreCase(parentCatalog.getCatalogType())) {
                    throw new GenericException(400, "El catálogo padre debe ser de tipo Primario.");
                }

                if (parentElementId != null) {
                    detailRepository.findById(parentElementId)
                            .filter(d -> d.getHeader().getId().equals(parentCatalogId))
                            .orElseThrow(() -> new GenericException(400, "Elemento padre no encontrado en el catálogo padre seleccionado."));
                }
            }
        }
    }
}








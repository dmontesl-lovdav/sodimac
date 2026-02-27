package com.sodimac.catman.api.service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.sodimac.catman.api.exception.GenericException;
import com.sodimac.catman.api.model.dto.CatalogCreateDto;
import com.sodimac.catman.api.model.dto.CatalogPageResponse;
import com.sodimac.catman.api.model.dto.CatalogResponseDto;
import com.sodimac.catman.api.model.dto.CatalogUpdateDto;
import com.sodimac.catman.api.model.entity.CatalogHeader;
import com.sodimac.catman.api.repository.CatalogDetailRepository;
import com.sodimac.catman.api.repository.CatalogHeaderRepository;

import jakarta.persistence.criteria.Predicate;

@Service
public class CatalogManagementServiceImpl implements CatalogManagementService {

    private final CatalogHeaderRepository headerRepository;
    private final CatalogDetailRepository detailRepository;

    public CatalogManagementServiceImpl(
            CatalogHeaderRepository headerRepository,
            CatalogDetailRepository detailRepository) {
        this.headerRepository = headerRepository;
        this.detailRepository = detailRepository;
    }

    @Override
    public CatalogPageResponse findCatalogs(
            Integer id,
            String name,
            String description,
            String catalogType,
            Integer status,
            String code,
            String prefix,
            Pageable pageable) {

        Specification<CatalogHeader> spec = buildSpecification(id, name, description, catalogType, status, code, prefix);
        Page<CatalogHeader> page = headerRepository.findAll(spec, pageable);

        List<CatalogResponseDto> items = page.getContent().stream()
                .map(this::toResponseDto)
                .collect(Collectors.toList());

        return CatalogPageResponse.builder()
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
    public Optional<CatalogResponseDto> findById(Integer id) {
        return headerRepository.findById(id).map(this::toResponseDto);
    }

    @Override
    @Transactional
    public CatalogResponseDto createCatalog(CatalogCreateDto createDto, String userId) {
        String normalizedType = normalizeType(createDto.getCatalogType());

        // Usar código y prefijo proporcionados por el usuario, o auto-generar si no se proporcionan
        String code = (createDto.getCode() != null && !createDto.getCode().isBlank()) 
                ? createDto.getCode().toUpperCase().trim() 
                : generateCode(createDto.getName());
        String prefix = (createDto.getPrefix() != null && !createDto.getPrefix().isBlank()) 
                ? createDto.getPrefix().toUpperCase().trim() 
                : generatePrefix(createDto.getName());

        if (headerRepository.findByCode(code).isPresent()) {
            throw new GenericException(409, "Ya existe un catálogo con el código: " + code);
        }

        if (headerRepository.findByPrefix(prefix).isPresent()) {
            throw new GenericException(409, "Ya existe un catálogo con el prefijo: " + prefix);
        }

        CatalogHeader header = CatalogHeader.builder()
                .code(code)
                .prefix(prefix)
                .name(createDto.getName())
                .description(createDto.getDescription())
                .catalogType(normalizedType)
                .module(createDto.getModule() != null ? createDto.getModule() : "general")
                .status(CatalogHeader.STATUS_INACTIVE) // Nuevo catálogo inicia inactivo
                .build();

        CatalogHeader saved = headerRepository.save(header);
        return toResponseDto(saved);
    }

    @Override
    @Transactional
    public CatalogResponseDto updateCatalog(Integer id, CatalogUpdateDto updateDto, String userId) {
        CatalogHeader header = headerRepository.findById(id)
                .orElseThrow(() -> new GenericException(404, "Catálogo no encontrado con ID: " + id));

        if (updateDto.getName() != null && !updateDto.getName().isBlank()) {
            header.setName(updateDto.getName());
        }

        if (updateDto.getDescription() != null) {
            header.setDescription(updateDto.getDescription());
        }

        if (updateDto.getCatalogType() != null && !updateDto.getCatalogType().isBlank()) {
            String normalizedType = normalizeType(updateDto.getCatalogType());
            header.setCatalogType(normalizedType);
        }

        if (updateDto.getStatus() != null) {
            header.setStatus(updateDto.getStatus());
        }

        if (updateDto.getModule() != null) {
            header.setModule(updateDto.getModule());
        }

        header.setUpdatedAt(LocalDateTime.now());

        CatalogHeader saved = headerRepository.save(header);
        return toResponseDto(saved);
    }

    private Specification<CatalogHeader> buildSpecification(
            Integer id, String name, String description, String catalogType, Integer status,
            String code, String prefix) {

        return (root, query, cb) -> {
            Predicate predicate = cb.conjunction();

            if (id != null) {
                predicate = cb.and(predicate, cb.equal(root.get("id"), id));
            }

            if (name != null && !name.isBlank()) {
                predicate = cb.and(predicate,
                        cb.like(cb.lower(root.get("name")), "%" + name.toLowerCase() + "%"));
            }

            if (description != null && !description.isBlank()) {
                predicate = cb.and(predicate,
                        cb.like(cb.lower(root.get("description")), "%" + description.toLowerCase() + "%"));
            }

            if (catalogType != null && !catalogType.isBlank()) {
                predicate = cb.and(predicate,
                        cb.equal(cb.upper(root.get("catalogType")), catalogType.toUpperCase()));
            }

            if (status != null) {
                predicate = cb.and(predicate, cb.equal(root.get("status"), status));
            }

            if (code != null && !code.isBlank()) {
                predicate = cb.and(predicate,
                        cb.like(cb.lower(root.get("code")), "%" + code.toLowerCase() + "%"));
            }

            if (prefix != null && !prefix.isBlank()) {
                predicate = cb.and(predicate,
                        cb.like(cb.lower(root.get("prefix")), "%" + prefix.toLowerCase() + "%"));
            }

            return predicate;
        };
    }

    private CatalogResponseDto toResponseDto(CatalogHeader header) {
        Long elementCount = detailRepository.countByHeaderId(header.getId());

        return CatalogResponseDto.builder()
                .id(header.getId())
                .code(header.getCode())
                .prefix(header.getPrefix())
                .name(header.getName())
                .description(header.getDescription())
                .module(header.getModule())
                .catalogType(header.getCatalogType())
                .status(header.getStatus())
                .createdAt(header.getCreatedAt())
                .updatedAt(header.getUpdatedAt())
                .elementCount(elementCount)
                .build();
    }

    private String normalizeType(String type) {
        if (type == null || type.isBlank()) {
            throw new GenericException(400, "El tipo de catálogo es obligatorio.");
        }

        String normalized = type.toUpperCase().trim();
        if (!normalized.equals("PRIMARIO") && !normalized.equals("SECUNDARIO")) {
            throw new GenericException(400, "El tipo de catálogo debe ser PRIMARIO o SECUNDARIO.");
        }

        return normalized;
    }

    private String generateCode(String name) {
        String base = "CAT_" + name.toUpperCase()
                .replaceAll("[^A-Z0-9]", "_")
                .replaceAll("_+", "_")
                .substring(0, Math.min(name.length(), 50));

        String code = base;
        int suffix = 1;
        while (headerRepository.findByCode(code).isPresent()) {
            code = base + "_" + suffix++;
        }

        return code;
    }

    private String generatePrefix(String name) {
        String clean = name.toUpperCase().replaceAll("[^A-Z]", "");
        if (clean.length() >= 3) {
            return clean.substring(0, 3);
        } else if (clean.length() > 0) {
            return String.format("%-3s", clean).replace(' ', 'X');
        }
        return "CAT";
    }
}


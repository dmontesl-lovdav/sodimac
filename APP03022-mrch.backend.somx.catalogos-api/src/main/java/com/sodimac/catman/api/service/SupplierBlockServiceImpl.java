package com.sodimac.catman.api.service;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.sodimac.catman.api.model.dto.CatalogDetailDto;
import com.sodimac.catman.api.model.dto.SupplierBlockCreateDto;
import com.sodimac.catman.api.model.dto.SupplierBlockDto;
import com.sodimac.catman.api.model.dto.SupplierBlockResponseDto;
import com.sodimac.catman.api.model.dto.SupplierBlockUpdateDto;
import com.sodimac.catman.api.model.entity.SupplierBlock;
import com.sodimac.catman.api.repository.SupplierBlockRepository;
import com.sodimac.catman.api.repository.SupplierRepository;

/**
 * Implementacion del servicio de bloqueos de proveedores.
 * STM-1224: Bloqueo de proveedores por rango de fechas.
 * STM-605: Integracion con catalogo de mensajes y validacion de proveedor.
 */
@Service
public class SupplierBlockServiceImpl implements SupplierBlockService {

    private static final Logger log = LoggerFactory.getLogger(SupplierBlockServiceImpl.class);

    // STM-605: Codigos de mensajes del catalogo
    private static final String MSG_DATE_INVALID = "WRN001";
    private static final String MSG_SUPPLIER_NOT_FOUND = "BUS215";
    private static final String MSG_BLOCK_OVERLAP = "BUS216";
    private static final String MSG_BLOCK_CREATED = "RES001";
    private static final Integer DEFAULT_LANG_ID = 1;

    private final SupplierBlockRepository supplierBlockRepository;
    private final SupplierRepository supplierRepository;
    private final CatalogHeaderService catalogHeaderService;

    public SupplierBlockServiceImpl(
            SupplierBlockRepository supplierBlockRepository,
            SupplierRepository supplierRepository,
            CatalogHeaderService catalogHeaderService) {
        this.supplierBlockRepository = supplierBlockRepository;
        this.supplierRepository = supplierRepository;
        this.catalogHeaderService = catalogHeaderService;
    }

    @Override
    public List<SupplierBlockDto> findAll() {
        log.debug("Obteniendo todos los bloqueos activos");
        List<SupplierBlock> blocks = supplierBlockRepository.findByStatus(SupplierBlock.STATUS_ACTIVE);
        return blocks.stream().map(this::toDto).collect(Collectors.toList());
    }

    @Override
    public List<SupplierBlockDto> findByStatus(Integer status) {
        log.debug("Obteniendo bloqueos por estado: {}", status);
        List<SupplierBlock> blocks = supplierBlockRepository.findByStatus(status);
        return blocks.stream().map(this::toDto).collect(Collectors.toList());
    }

    @Override
    public Optional<SupplierBlockDto> findById(Integer id) {
        log.debug("Buscando bloqueo por ID: {}", id);
        return supplierBlockRepository.findById(id).map(this::toDto);
    }

    @Override
    public List<SupplierBlockDto> findBySupplierNumber(String supplierNumber) {
        log.debug("Buscando bloqueos para proveedor: {}", supplierNumber);
        List<SupplierBlock> blocks = supplierBlockRepository.findBySupplierNumber(supplierNumber);
        return blocks.stream().map(this::toDto).collect(Collectors.toList());
    }

    @Override
    public List<SupplierBlockDto> findCurrentActiveBlocks(String supplierNumber) {
        log.debug("Buscando bloqueos activos vigentes para proveedor: {}", supplierNumber);
        List<SupplierBlock> blocks = supplierBlockRepository.findCurrentActiveBlocks(supplierNumber);
        return blocks.stream().map(this::toDto).collect(Collectors.toList());
    }

    @Override
    public boolean isSupplierCurrentlyBlocked(String supplierNumber) {
        log.debug("Verificando si proveedor esta bloqueado: {}", supplierNumber);
        return supplierBlockRepository.isSupplierCurrentlyBlocked(supplierNumber);
    }

    @Override
    public List<SupplierBlockDto> findActiveBlocksAtDate(String supplierNumber, LocalDate date) {
        log.debug("Buscando bloqueos activos para proveedor {} a la fecha {}", supplierNumber, date);
        List<SupplierBlock> blocks = supplierBlockRepository.findActiveBlocksAtDate(supplierNumber, date);
        return blocks.stream().map(this::toDto).collect(Collectors.toList());
    }

    @Override
    @Transactional
    public SupplierBlockResponseDto create(SupplierBlockCreateDto dto, String createdBy) {
        log.info("Creando bloqueo para proveedor: {}", dto.getSupplierNumber());

        // STM-605: Validar que el proveedor exista
        if (!supplierRepository.existsBySupplierNumber(dto.getSupplierNumber())) {
            throw new IllegalArgumentException(getMessage(MSG_SUPPLIER_NOT_FOUND, dto.getSupplierNumber()));
        }

        // Validar fechas
        if (dto.getValidTo().isBefore(dto.getValidFrom())) {
            throw new IllegalArgumentException(getMessage(MSG_DATE_INVALID));
        }

        // Verificar si hay bloqueos que se solapan
        List<SupplierBlock> overlapping = supplierBlockRepository.findOverlappingBlocks(
                dto.getSupplierNumber(), dto.getValidFrom(), dto.getValidTo());
        if (!overlapping.isEmpty()) {
            throw new IllegalArgumentException(getMessage(MSG_BLOCK_OVERLAP, dto.getSupplierNumber()));
        }

        SupplierBlock block = SupplierBlock.builder()
                .supplierNumber(dto.getSupplierNumber())
                .validFrom(dto.getValidFrom())
                .validTo(dto.getValidTo())
                .blockReason(dto.getBlockReason())
                .status(SupplierBlock.STATUS_ACTIVE)
                .createdBy(createdBy)
                .build();

        SupplierBlock saved = supplierBlockRepository.save(block);
        log.info("Bloqueo creado con ID: {}", saved.getId());

        return SupplierBlockResponseDto.builder()
                .message(getMessage(MSG_BLOCK_CREATED, dto.getSupplierNumber()))
                .data(toDto(saved))
                .build();
    }

    @Override
    @Transactional
    public Optional<SupplierBlockResponseDto> update(Integer id, SupplierBlockUpdateDto dto, String updatedBy) {
        log.info("Actualizando bloqueo ID: {}", id);

        return supplierBlockRepository.findById(id)
                .map(block -> {
                    LocalDate validFrom = dto.getValidFrom() != null ? dto.getValidFrom() : block.getValidFrom();
                    LocalDate validTo = dto.getValidTo() != null ? dto.getValidTo() : block.getValidTo();

                    // Validar fechas
                    if (validTo.isBefore(validFrom)) {
                        throw new IllegalArgumentException(getMessage(MSG_DATE_INVALID));
                    }

                    // Verificar solapamiento si cambian las fechas (excluyendo el bloqueo actual)
                    if (dto.getValidFrom() != null || dto.getValidTo() != null) {
                        List<SupplierBlock> overlapping = supplierBlockRepository.findOverlappingBlocks(
                                block.getSupplierNumber(), validFrom, validTo);
                        overlapping.removeIf(b -> b.getId().equals(id));
                        if (!overlapping.isEmpty()) {
                            throw new IllegalArgumentException(getMessage(MSG_BLOCK_OVERLAP, block.getSupplierNumber()));
                        }
                    }

                    if (dto.getValidFrom() != null) {
                        block.setValidFrom(dto.getValidFrom());
                    }
                    if (dto.getValidTo() != null) {
                        block.setValidTo(dto.getValidTo());
                    }
                    if (dto.getBlockReason() != null) {
                        block.setBlockReason(dto.getBlockReason());
                    }
                    if (dto.getStatus() != null) {
                        block.setStatus(dto.getStatus());
                    }

                    block.setUpdatedBy(updatedBy);
                    SupplierBlock saved = supplierBlockRepository.save(block);
                    log.info("Bloqueo actualizado: {}", saved.getId());

                    return SupplierBlockResponseDto.builder()
                            .message(getMessage(MSG_BLOCK_CREATED, block.getSupplierNumber()))
                            .data(toDto(saved))
                            .build();
                });
    }

    @Override
    @Transactional
    public boolean delete(Integer id, String updatedBy) {
        log.info("Eliminando (desactivando) bloqueo ID: {}", id);

        return supplierBlockRepository.findById(id)
                .map(block -> {
                    block.setStatus(SupplierBlock.STATUS_INACTIVE);
                    block.setUpdatedBy(updatedBy);
                    supplierBlockRepository.save(block);
                    log.info("Bloqueo desactivado: {}", id);
                    return true;
                })
                .orElse(false);
    }

    /**
     * Obtiene un mensaje del catalogo con sustitucion de parametros.
     * STM-605: Patron copiado de SupplierServiceImpl.
     */
    private String getMessage(String key, String... params) {
        Optional<CatalogDetailDto> detail = catalogHeaderService.findDetailByKeyFormatted(
                key, DEFAULT_LANG_ID, List.of(params));
        return detail.map(CatalogDetailDto::getDescription)
                .orElse("Error: " + key);
    }

    /**
     * Convierte una entidad SupplierBlock a DTO.
     */
    private SupplierBlockDto toDto(SupplierBlock entity) {
        return SupplierBlockDto.builder()
                .id(entity.getId())
                .supplierNumber(entity.getSupplierNumber())
                .validFrom(entity.getValidFrom())
                .validTo(entity.getValidTo())
                .blockReason(entity.getBlockReason())
                .status(entity.getStatus())
                .currentlyBlocked(entity.isCurrentlyBlocked())
                .createdAt(entity.getCreatedAt())
                .createdBy(entity.getCreatedBy())
                .updatedAt(entity.getUpdatedAt())
                .updatedBy(entity.getUpdatedBy())
                .build();
    }
}

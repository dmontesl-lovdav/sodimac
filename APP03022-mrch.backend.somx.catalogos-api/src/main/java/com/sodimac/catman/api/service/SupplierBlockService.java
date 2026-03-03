package com.sodimac.catman.api.service;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import com.sodimac.catman.api.model.dto.SupplierBlockCreateDto;
import com.sodimac.catman.api.model.dto.SupplierBlockDto;
import com.sodimac.catman.api.model.dto.SupplierBlockResponseDto;
import com.sodimac.catman.api.model.dto.SupplierBlockUpdateDto;

/**
 * Servicio para gestion de bloqueos de proveedores.
 * STM-1224: Bloqueo de proveedores por rango de fechas.
 */
public interface SupplierBlockService {

    /**
     * Obtiene todos los bloqueos activos.
     */
    List<SupplierBlockDto> findAll();

    /**
     * Obtiene todos los bloqueos por estado.
     */
    List<SupplierBlockDto> findByStatus(Integer status);

    /**
     * Obtiene un bloqueo por su ID.
     */
    Optional<SupplierBlockDto> findById(Integer id);

    /**
     * Obtiene los bloqueos de un proveedor.
     */
    List<SupplierBlockDto> findBySupplierNumber(String supplierNumber);

    /**
     * Obtiene los bloqueos activos vigentes de un proveedor.
     */
    List<SupplierBlockDto> findCurrentActiveBlocks(String supplierNumber);

    /**
     * Verifica si un proveedor esta actualmente bloqueado.
     */
    boolean isSupplierCurrentlyBlocked(String supplierNumber);

    /**
     * Obtiene los bloqueos activos a una fecha especifica.
     */
    List<SupplierBlockDto> findActiveBlocksAtDate(String supplierNumber, LocalDate date);

    /**
     * Crea un nuevo bloqueo de proveedor.
     * STM-605: Retorna respuesta con mensaje del catalogo.
     */
    SupplierBlockResponseDto create(SupplierBlockCreateDto dto, String createdBy);

    /**
     * Actualiza un bloqueo existente.
     * STM-605: Retorna respuesta con mensaje del catalogo.
     */
    Optional<SupplierBlockResponseDto> update(Integer id, SupplierBlockUpdateDto dto, String updatedBy);

    /**
     * Elimina (desactiva) un bloqueo.
     */
    boolean delete(Integer id, String updatedBy);
}

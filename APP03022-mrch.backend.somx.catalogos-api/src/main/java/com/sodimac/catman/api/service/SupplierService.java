package com.sodimac.catman.api.service;

import java.util.List;
import java.util.Optional;

import com.sodimac.catman.api.model.dto.PaymentConditionDto;
import com.sodimac.catman.api.model.dto.SupplierCreateDto;
import com.sodimac.catman.api.model.dto.SupplierDto;
import com.sodimac.catman.api.model.dto.SupplierFilterDto;
import com.sodimac.catman.api.model.dto.SupplierTypeDto;
import com.sodimac.catman.api.model.dto.SupplierUpdateDto;

/**
 * Servicio para gestion de proveedores.
 */
public interface SupplierService {

    /**
     * Obtiene todos los proveedores activos.
     */
    List<SupplierDto> findAll();

    /**
     * Obtiene todos los proveedores por estado.
     */
    List<SupplierDto> findByStatus(Integer status);

    /**
     * Obtiene un proveedor por su ID.
     */
    Optional<SupplierDto> findById(Integer id);

    /**
     * Obtiene un proveedor por su numero.
     */
    Optional<SupplierDto> findBySupplierNumber(String supplierNumber);

    /**
     * Obtiene un proveedor por su RFC.
     */
    Optional<SupplierDto> findByRfc(String rfc);

    /**
     * Crea un nuevo proveedor.
     */
    SupplierDto create(SupplierCreateDto dto, String createdBy);

    /**
     * Actualiza un proveedor existente.
     */
    Optional<SupplierDto> update(Integer id, SupplierUpdateDto dto, String updatedBy);

    /**
     * Elimina (desactiva) un proveedor.
     */
    boolean delete(Integer id, String updatedBy);

    /**
     * Obtiene todos los tipos de proveedor activos.
     */
    List<SupplierTypeDto> findAllSupplierTypes();

    /**
     * Obtiene todas las condiciones de pago activas.
     */
    List<PaymentConditionDto> findAllPaymentConditions();

    /**
     * STM-831: Filtra proveedores por tipo y estado de bloqueo.
     *
     * @param tipoProveedor   ID del tipo de proveedor (0=todos, 1=mercancia, 2=transporte, 3=indirectos, 4=servicios)
     * @param estatusBloqueo  Estado de bloqueo (0=solo bloqueados, 1=solo activos, 2=todos)
     * @return Lista de proveedores filtrados con informacion de bloqueo
     */
    List<SupplierFilterDto> findByTypeAndBlockStatus(Integer tipoProveedor, Integer estatusBloqueo);
}

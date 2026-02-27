package com.sodimac.catman.api.service;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.sodimac.catman.api.mapper.SupplierMapper;
import com.sodimac.catman.api.model.dto.CatalogDetailDto;
import com.sodimac.catman.api.model.dto.PaymentConditionDto;
import com.sodimac.catman.api.model.dto.SupplierCreateDto;
import com.sodimac.catman.api.model.dto.SupplierDto;
import com.sodimac.catman.api.model.dto.SupplierFilterDto;
import com.sodimac.catman.api.model.dto.SupplierTypeDto;
import com.sodimac.catman.api.model.dto.SupplierUpdateDto;
import com.sodimac.catman.api.model.entity.PaymentCondition;
import com.sodimac.catman.api.model.entity.Supplier;
import com.sodimac.catman.api.model.entity.SupplierBlock;
import com.sodimac.catman.api.model.entity.SupplierType;
import com.sodimac.catman.api.repository.PaymentConditionRepository;
import com.sodimac.catman.api.repository.SupplierBlockRepository;
import com.sodimac.catman.api.repository.SupplierRepository;
import com.sodimac.catman.api.repository.SupplierTypeRepository;

@Service
public class SupplierServiceImpl implements SupplierService {

    private static final Logger log = LoggerFactory.getLogger(SupplierServiceImpl.class);

    // Claves de mensajes del catalogo (suppliers-api)
    private static final String MSG_SUPPLIER_DUPLICATE = "BUS200";     // Ya existe proveedor con numero
    private static final String MSG_SUPPLIER_TYPE_NOT_FOUND = "BUS204"; // Tipo de proveedor no encontrado
    private static final String MSG_PAYMENT_CONDITION_NOT_FOUND = "BUS205"; // Condicion de pago no encontrada
    private static final String MSG_SUPPLIER_TYPE_INVALID = "BUS214";  // Tipo de proveedor no valido (STM-831)

    // Constantes para filtrado de proveedores (STM-831)
    private static final int MIN_SUPPLIER_TYPE = 0;
    private static final int MAX_SUPPLIER_TYPE = 4;
    private static final int BLOCK_STATUS_BLOCKED_ONLY = 0;
    private static final int BLOCK_STATUS_ACTIVE_ONLY = 1;
    private static final int BLOCK_STATUS_ALL = 2;

    // Idioma por defecto (Español)
    private static final Integer DEFAULT_LANG_ID = 1;

    private final SupplierRepository supplierRepository;
    private final SupplierTypeRepository supplierTypeRepository;
    private final PaymentConditionRepository paymentConditionRepository;
    private final SupplierBlockRepository supplierBlockRepository;
    private final SupplierMapper supplierMapper;
    private final CatalogHeaderService catalogHeaderService;

    public SupplierServiceImpl(
            SupplierRepository supplierRepository,
            SupplierTypeRepository supplierTypeRepository,
            PaymentConditionRepository paymentConditionRepository,
            SupplierBlockRepository supplierBlockRepository,
            SupplierMapper supplierMapper,
            CatalogHeaderService catalogHeaderService) {
        this.supplierRepository = supplierRepository;
        this.supplierTypeRepository = supplierTypeRepository;
        this.paymentConditionRepository = paymentConditionRepository;
        this.supplierBlockRepository = supplierBlockRepository;
        this.supplierMapper = supplierMapper;
        this.catalogHeaderService = catalogHeaderService;
    }

    /**
     * Obtiene un mensaje del catalogo formateado con los parametros proporcionados.
     * Si no se encuentra el mensaje, retorna un mensaje por defecto.
     */
    private String getMessage(String key, String... params) {
        Optional<CatalogDetailDto> detail = catalogHeaderService.findDetailByKeyFormatted(
                key, DEFAULT_LANG_ID, List.of(params));
        return detail.map(CatalogDetailDto::getDescription)
                .orElse("Error: " + key);
    }

    @Override
    public List<SupplierDto> findAll() {
        log.debug("Obteniendo todos los proveedores activos");
        List<Supplier> suppliers = supplierRepository.findByStatus(Supplier.STATUS_ACTIVE);
        return supplierMapper.toDtoList(suppliers);
    }

    @Override
    public List<SupplierDto> findByStatus(Integer status) {
        log.debug("Obteniendo proveedores por estado: {}", status);
        List<Supplier> suppliers = supplierRepository.findByStatus(status);
        return supplierMapper.toDtoList(suppliers);
    }

    @Override
    public Optional<SupplierDto> findById(Integer id) {
        log.debug("Buscando proveedor por ID: {}", id);
        return supplierRepository.findById(id)
                .map(supplierMapper::toDto);
    }

    @Override
    public Optional<SupplierDto> findBySupplierNumber(String supplierNumber) {
        log.debug("Buscando proveedor por numero: {}", supplierNumber);
        return supplierRepository.findBySupplierNumber(supplierNumber)
                .map(supplierMapper::toDto);
    }

    @Override
    public Optional<SupplierDto> findByRfc(String rfc) {
        log.debug("Buscando proveedor por RFC: {}", rfc);
        return supplierRepository.findByRfc(rfc)
                .map(supplierMapper::toDto);
    }

    @Override
    @Transactional
    public SupplierDto create(SupplierCreateDto dto, String createdBy) {
        log.info("Creando proveedor: {}", dto.getSupplierNumber());

        // Validar que no exista el numero de proveedor
        if (supplierRepository.existsBySupplierNumber(dto.getSupplierNumber())) {
            throw new IllegalArgumentException(getMessage(MSG_SUPPLIER_DUPLICATE, dto.getSupplierNumber()));
        }

        Supplier supplier = supplierMapper.toEntity(dto);
        supplier.setCreatedBy(createdBy);

        // Asignar tipo de proveedor si se especifico
        if (dto.getSupplierTypeId() != null) {
            SupplierType supplierType = supplierTypeRepository.findById(dto.getSupplierTypeId())
                    .orElseThrow(() -> new IllegalArgumentException(
                            getMessage(MSG_SUPPLIER_TYPE_NOT_FOUND, dto.getSupplierTypeId().toString())));
            supplier.setSupplierType(supplierType);
        }

        // Asignar condicion de pago si se especifico
        if (dto.getPaymentConditionId() != null) {
            PaymentCondition paymentCondition = paymentConditionRepository.findById(dto.getPaymentConditionId())
                    .orElseThrow(() -> new IllegalArgumentException(
                            getMessage(MSG_PAYMENT_CONDITION_NOT_FOUND, dto.getPaymentConditionId().toString())));
            supplier.setPaymentCondition(paymentCondition);
        }

        Supplier saved = supplierRepository.save(supplier);
        log.info("Proveedor creado con ID: {}", saved.getId());

        return supplierMapper.toDto(saved);
    }

    @Override
    @Transactional
    public Optional<SupplierDto> update(Integer id, SupplierUpdateDto dto, String updatedBy) {
        log.info("Actualizando proveedor ID: {}", id);

        return supplierRepository.findById(id)
                .map(supplier -> {
                    if (dto.getRfc() != null) {
                        supplier.setRfc(dto.getRfc());
                    }
                    if (dto.getBusinessName() != null) {
                        supplier.setBusinessName(dto.getBusinessName());
                    }
                    if (dto.getLogo() != null) {
                        supplier.setLogo(dto.getLogo());
                    }
                    if (dto.getStatus() != null) {
                        supplier.setStatus(dto.getStatus());
                    }

                    // Actualizar tipo de proveedor
                    if (dto.getSupplierTypeId() != null) {
                        SupplierType supplierType = supplierTypeRepository.findById(dto.getSupplierTypeId())
                                .orElseThrow(() -> new IllegalArgumentException(
                                        getMessage(MSG_SUPPLIER_TYPE_NOT_FOUND, dto.getSupplierTypeId().toString())));
                        supplier.setSupplierType(supplierType);
                    }

                    // Actualizar condicion de pago
                    if (dto.getPaymentConditionId() != null) {
                        PaymentCondition paymentCondition = paymentConditionRepository.findById(dto.getPaymentConditionId())
                                .orElseThrow(() -> new IllegalArgumentException(
                                        getMessage(MSG_PAYMENT_CONDITION_NOT_FOUND, dto.getPaymentConditionId().toString())));
                        supplier.setPaymentCondition(paymentCondition);
                    }

                    supplier.setUpdatedBy(updatedBy);
                    Supplier saved = supplierRepository.save(supplier);
                    log.info("Proveedor actualizado: {}", saved.getId());

                    return supplierMapper.toDto(saved);
                });
    }

    @Override
    @Transactional
    public boolean delete(Integer id, String updatedBy) {
        log.info("Eliminando (desactivando) proveedor ID: {}", id);

        return supplierRepository.findById(id)
                .map(supplier -> {
                    supplier.setStatus(Supplier.STATUS_INACTIVE);
                    supplier.setUpdatedBy(updatedBy);
                    supplierRepository.save(supplier);
                    log.info("Proveedor desactivado: {}", id);
                    return true;
                })
                .orElse(false);
    }

    @Override
    public List<SupplierTypeDto> findAllSupplierTypes() {
        log.debug("Obteniendo todos los tipos de proveedor activos");
        List<SupplierType> types = supplierTypeRepository.findByStatus(SupplierType.STATUS_ACTIVE);
        return supplierMapper.toSupplierTypeDtoList(types);
    }

    @Override
    public List<PaymentConditionDto> findAllPaymentConditions() {
        log.debug("Obteniendo todas las condiciones de pago activas");
        List<PaymentCondition> conditions = paymentConditionRepository.findByStatus(PaymentCondition.STATUS_ACTIVE);
        return supplierMapper.toPaymentConditionDtoList(conditions);
    }

    /**
     * STM-831: Filtra proveedores por tipo y estado de bloqueo.
     */
    @Override
    @Transactional(readOnly = true)
    public List<SupplierFilterDto> findByTypeAndBlockStatus(Integer tipoProveedor, Integer estatusBloqueo) {
        log.info("Filtrando proveedores - tipoProveedor: {}, estatusBloqueo: {}", tipoProveedor, estatusBloqueo);

        // Validar tipo de proveedor (0-4)
        if (tipoProveedor < MIN_SUPPLIER_TYPE || tipoProveedor > MAX_SUPPLIER_TYPE) {
            throw new IllegalArgumentException(getMessage(MSG_SUPPLIER_TYPE_INVALID, tipoProveedor.toString()));
        }

        // Obtener proveedores filtrados por tipo
        List<Supplier> suppliers = supplierRepository.findByTypeFilter(tipoProveedor);

        // Procesar cada proveedor y determinar su estado de bloqueo
        List<SupplierFilterDto> result = new ArrayList<>();

        for (Supplier supplier : suppliers) {
            // Obtener bloqueos activos vigentes del proveedor
            List<SupplierBlock> activeBlocks = supplierBlockRepository.findCurrentActiveBlocks(supplier.getSupplierNumber());
            boolean isBlocked = !activeBlocks.isEmpty();
            SupplierBlock activeBlock = isBlocked ? activeBlocks.get(0) : null;

            // Filtrar segun estatusBloqueo
            boolean includeSupplier = switch (estatusBloqueo) {
                case BLOCK_STATUS_BLOCKED_ONLY -> isBlocked;      // Solo bloqueados
                case BLOCK_STATUS_ACTIVE_ONLY -> !isBlocked;      // Solo activos (no bloqueados)
                case BLOCK_STATUS_ALL -> true;                     // Todos
                default -> true;
            };

            if (includeSupplier) {
                result.add(supplierMapper.toFilterDto(supplier, isBlocked, activeBlock));
            }
        }

        log.info("Proveedores encontrados: {}", result.size());
        return result;
    }
}

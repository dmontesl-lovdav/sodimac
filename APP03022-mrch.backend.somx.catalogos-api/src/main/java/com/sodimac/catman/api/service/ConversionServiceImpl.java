package com.sodimac.catman.api.service;

import com.sodimac.catman.api.exception.GenericException;
import com.sodimac.catman.api.model.dto.*;
import com.sodimac.catman.api.model.entity.CatalogConversion;
import com.sodimac.catman.api.model.entity.CatalogDetail;
import com.sodimac.catman.api.repository.CatalogConversionRepository;
import com.sodimac.catman.api.repository.CatalogDetailRepository;
import jakarta.persistence.criteria.Predicate;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class ConversionServiceImpl implements ConversionService {

    private final CatalogConversionRepository convRepo;
    private final CatalogDetailRepository detailRepo;

    public ConversionServiceImpl(CatalogConversionRepository convRepo, CatalogDetailRepository detailRepo) {
        this.convRepo = convRepo;
        this.detailRepo = detailRepo;
    }

    @Override
    @Transactional(readOnly = true)
    public ConversionPageResponse search(Integer sourceElementId, Integer targetElementId, String elemento,
                                          String valor, String catalogoOrigen, Integer estatus, Pageable pageable) {
        Specification<CatalogConversion> spec = (root, query, cb) -> {
            List<Predicate> preds = new ArrayList<>();
            if (sourceElementId != null) preds.add(cb.equal(root.get("sourceElement").get("id"), sourceElementId));
            if (targetElementId != null) preds.add(cb.equal(root.get("targetElement").get("id"), targetElementId));
            if (elemento != null && !elemento.isBlank())
                preds.add(cb.like(cb.lower(root.get("targetElement").get("key")), "%" + elemento.toLowerCase() + "%"));
            if (valor != null && !valor.isBlank())
                preds.add(cb.like(cb.lower(root.get("targetElement").get("value")), "%" + valor.toLowerCase() + "%"));
            if (catalogoOrigen != null && !catalogoOrigen.isBlank())
                preds.add(cb.like(cb.lower(root.get("targetElement").get("header").get("name")), "%" + catalogoOrigen.toLowerCase() + "%"));
            if (estatus != null) preds.add(cb.equal(root.get("status"), estatus));
            return cb.and(preds.toArray(new Predicate[0]));
        };

        Page<CatalogConversion> page = convRepo.findAll(spec, pageable);
        List<ConversionDto> items = page.getContent().stream().map(this::toDto).collect(Collectors.toList());

        return ConversionPageResponse.builder()
                .items(items).page(page.getNumber() + 1).pageSize(page.getSize())
                .total(page.getTotalElements()).totalPages(page.getTotalPages()).build();
    }

    @Override
    @Transactional(readOnly = true)
    public ConversionDto getById(Integer id) {
        CatalogConversion conv = convRepo.findById(id)
                .orElseThrow(() -> new GenericException(404, "Conversión no encontrada: " + id));
        return toDto(conv);
    }

    @Override
    @Transactional
    public ConversionDto create(ConversionCreateDto dto, String userId) {
        CatalogDetail source = detailRepo.findById(dto.getSourceElementId())
                .orElseThrow(() -> new GenericException(404, "Elemento origen no encontrado: " + dto.getSourceElementId()));
        CatalogDetail target = detailRepo.findById(dto.getTargetElementId())
                .orElseThrow(() -> new GenericException(404, "Elemento destino no encontrado: " + dto.getTargetElementId()));

        // CA-04: Validar duplicidad
        if (convRepo.existsBySourceElementIdAndTargetElementId(dto.getSourceElementId(), dto.getTargetElementId()))
            throw new GenericException(409, "El elemento " + target.getKey() + " del catálogo " + target.getHeader().getName() + " ya se encuentra agregado como conversión, seleccione otro elemento.");

        // Validar vigencias
        if (dto.getValidFrom() != null && dto.getValidTo() != null && dto.getValidTo().isBefore(dto.getValidFrom()))
            throw new GenericException(400, "La fecha de fin de vigencia no puede ser anterior a la de inicio.");

        // CA-03/CA-17: Auto-marcar como principal si no hay principal activa
        boolean shouldBePrincipal = dto.getIsPrincipal() != null && dto.getIsPrincipal();
        boolean noPrincipalExists = convRepo.findBySourceElementIdAndIsPrincipalTrue(dto.getSourceElementId()).isEmpty();
        if (noPrincipalExists) {
            shouldBePrincipal = true;
        }

        if (shouldBePrincipal) {
            convRepo.clearPrincipalBySourceElement(dto.getSourceElementId(), userId);
        }

        CatalogConversion conv = CatalogConversion.builder()
                .sourceElement(source).targetElement(target)
                .validFrom(dto.getValidFrom()).validTo(dto.getValidTo())
                .status(shouldBePrincipal ? CatalogConversion.STATUS_ACTIVE : CatalogConversion.STATUS_INACTIVE)
                .isPrincipal(shouldBePrincipal)
                .createdBy(userId).build();

        return toDto(convRepo.save(conv));
    }

    @Override
    @Transactional
    public ConversionDto update(Integer id, ConversionUpdateDto dto, String userId) {
        CatalogConversion conv = convRepo.findById(id)
                .orElseThrow(() -> new GenericException(404, "Conversión no encontrada: " + id));

        CatalogDetail newTarget = detailRepo.findById(dto.getTargetElementId())
                .orElseThrow(() -> new GenericException(404, "Elemento destino no encontrado: " + dto.getTargetElementId()));

        // Validar duplicidad (que la nueva combinación no exista en OTRA conversión)
        Integer sourceId = conv.getSourceElement().getId();
        if (!conv.getTargetElement().getId().equals(dto.getTargetElementId())) {
            if (convRepo.existsBySourceElementIdAndTargetElementId(sourceId, dto.getTargetElementId()))
                throw new GenericException(409, "El elemento " + newTarget.getKey() + " del catálogo " + newTarget.getHeader().getName() + " ya se encuentra agregado como conversión, seleccione otro elemento.");
        }

        // Validar vigencias
        if (dto.getValidFrom() != null && dto.getValidTo() != null && dto.getValidTo().isBefore(dto.getValidFrom()))
            throw new GenericException(400, "La fecha de fin de vigencia no puede ser anterior a la de inicio.");

        conv.setTargetElement(newTarget);
        if (dto.getValidFrom() != null) conv.setValidFrom(dto.getValidFrom());
        if (dto.getValidTo() != null) conv.setValidTo(dto.getValidTo());
        if (dto.getStatus() != null) conv.setStatus(dto.getStatus());
        // CA-14: Si es principal, mantenerla activa
        if (Boolean.TRUE.equals(conv.getIsPrincipal())) {
            conv.setStatus(CatalogConversion.STATUS_ACTIVE);
        }
        conv.setUpdatedBy(userId);

        return toDto(convRepo.save(conv));
    }

    @Override
    @Transactional
    public ConversionDto setPrincipal(Integer id, Boolean isPrincipal, String userId) {
        CatalogConversion conv = convRepo.findById(id)
                .orElseThrow(() -> new GenericException(404, "Conversión no encontrada: " + id));

        if (Boolean.TRUE.equals(isPrincipal)) {
            convRepo.clearPrincipalBySourceElement(conv.getSourceElement().getId(), userId);
            conv.setIsPrincipal(true);
            conv.setStatus(CatalogConversion.STATUS_ACTIVE);
        } else {
            conv.setIsPrincipal(false);
            conv.setStatus(CatalogConversion.STATUS_INACTIVE);
        }
        conv.setUpdatedBy(userId);
        return toDto(convRepo.save(conv));
    }

    @Override
    @Transactional
    public void delete(Integer id) {
        if (!convRepo.existsById(id)) throw new GenericException(404, "Conversión no encontrada: " + id);
        convRepo.deleteById(id);
    }

    @Override
    @Transactional
    public void deleteMultiple(List<Integer> ids) {
        convRepo.deleteAllByIdInBatch(ids);
    }

    private ConversionDto toDto(CatalogConversion c) {
        CatalogDetail src = c.getSourceElement();
        CatalogDetail tgt = c.getTargetElement();
        return ConversionDto.builder()
                .idConversion(c.getId())
                .idElementoOrigen(src.getId()).elementoOrigen(src.getKey())
                .valorElementoOrigen(src.getValue())
                .estatusElementoOrigen(src.getStatus() == 1 ? "Activo" : "Inactivo")
                .catalogoElementoOrigen(src.getHeader().getName())
                .idElemento(tgt.getId()).elemento(tgt.getKey()).valor(tgt.getValue())
                .catalogoOrigen(tgt.getHeader().getName())
                .fechaInicioVigencia(c.getValidFrom()).fechaFinVigencia(c.getValidTo())
                .estatus(c.getStatus() == 1 ? "Activo" : "Inactivo")
                .esPrincipal(c.getIsPrincipal())
                .idUsuarioRegistro(c.getCreatedBy()).fechaRegistro(c.getCreatedAt())
                .idUsuarioActualizacion(c.getUpdatedBy()).fechaActualizacion(c.getUpdatedAt())
                .build();
    }
}


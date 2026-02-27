package com.sodimac.aclaraciones.api.service.command.impl;

import java.util.Base64;
import java.util.Objects;

import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.server.ResponseStatusException;

import com.sodimac.aclaraciones.api.exception.GenericException;
import com.sodimac.aclaraciones.api.model.dto.FaqCategoryDto;
import com.sodimac.aclaraciones.api.model.dto.FaqCategoryResponse;
import com.sodimac.aclaraciones.api.model.entity.FaqCategory;
import com.sodimac.aclaraciones.api.repository.FaqCategoryRepository;
import com.sodimac.aclaraciones.api.service.category.FaqCategoryMapper;
import com.sodimac.aclaraciones.api.service.command.FaqCategoryCommandService;

@Service
@Transactional
public class FaqCategoryCommandServiceImpl implements FaqCategoryCommandService {

    private final FaqCategoryRepository categories;

    public FaqCategoryCommandServiceImpl(FaqCategoryRepository categories) {
        this.categories = categories;
    }

    /* ---------------- publish / unpublish ---------------- */
    @Override
    public FaqCategoryResponse updatePublication(Long id, Boolean published) {
        FaqCategory cat = categories.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Category not found"));

        cat.setIsActive(Boolean.TRUE.equals(published));
        categories.save(cat);

        return new FaqCategoryResponse(cat.getId(), cat.getName(), cat.getDescription(), cat.getIsActive());
    }

    /* ---------------- delete ---------------- */
    @Override
    public void deleteCategory(Long id) {
        if (!categories.existsById(id)) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Category not found");
        }
        categories.deleteById(id);
    }

    /* ---------------- update (JSON) ---------------- */
    @Override
    public FaqCategoryResponse updatePublication(Long categoryId, FaqCategoryDto target) throws GenericException {
        FaqCategory cat = categories.findById(categoryId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Category not found"));

        FaqCategoryMapper.applyCommon(cat, target); // name, description, isActive
        applyIconFromDto(cat, target); // decode Base64 -> byte[]
        categories.save(cat);

        return new FaqCategoryResponse(categoryId, cat.getName(), cat.getDescription(), cat.getIsActive());
    }

    /* ---------------- create (JSON) ---------------- */
    @Override
    public FaqCategoryResponse createPublication(FaqCategoryDto target) throws GenericException {
        if (this.categories.countByNameIgnoreCase(target.name()) > 0) {
            throw new ResponseStatusException(
                    HttpStatus.CONFLICT,
                    "Ya existe una categoría con ese nombre.");
        }

        FaqCategory cat = new FaqCategory();
        FaqCategoryMapper.applyCommon(cat, target);
        applyIconFromDto(cat, target); // decode Base64 -> byte[]
        this.categories.save(cat);

        return new FaqCategoryResponse(cat.getId(), cat.getName(), cat.getDescription(), cat.getIsActive());
    }

    /* ---------------- create (MULTIPART) ---------------- */
    @Override
    public FaqCategoryResponse createPublication(FaqCategoryDto target, MultipartFile iconFile)
            throws GenericException {
        target = withIconFromFile(target, iconFile); // pone base64 en DTO
        return createPublication(target); // aquí se decodifica
    }

    /* ---------------- update (MULTIPART) ---------------- */
    @Override
    public FaqCategoryResponse updatePublication(Long categoryId, FaqCategoryDto target, MultipartFile iconFile)
            throws GenericException {
        target = withIconFromFile(target, iconFile);
        return updatePublication(categoryId, target);
    }

    /* ==================== helpers ==================== */

    /**
     * Aplica el ícono del DTO a la entidad:
     * - icon == null -> no tocar
     * - icon == "" -> borrar imagen (set null)
     * - icon (base64) -> decodificar a byte[] y guardar nombre
     */
    private void applyIconFromDto(FaqCategory cat, FaqCategoryDto dto) {
        if (dto.icon() == null) {
            return; // no cambia el ícono
        }

        String s = dto.icon().trim();
        if (s.isEmpty()) {
            cat.setImageData(null);
            cat.setImageName(null);
            cat.setIconPath(null);
            return;
        }

        try {
            byte[] bytes = Base64.getDecoder().decode(s);
            cat.setImageData(bytes);
            cat.setImageName(dto.iconName());
            cat.setIconPath(null); // usando blob
        } catch (IllegalArgumentException ex) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Invalid base64 icon", ex);
        }
    }

    /**
     * Convierte el archivo a Base64 dentro del DTO (se decodifica al persistir).
     */
    private FaqCategoryDto withIconFromFile(FaqCategoryDto dto, MultipartFile file) {
        if (file == null)
            return dto;

        try {
            if (file.isEmpty()) {
                return new FaqCategoryDto(dto.id(), dto.name(), dto.description(), "", "", dto.isActive());
            }
            String b64 = Base64.getEncoder().encodeToString(file.getBytes());
            String safe = sanitize(Objects.toString(file.getOriginalFilename(), "icon.bin"));
            return new FaqCategoryDto(dto.id(), dto.name(), dto.description(), b64, safe, dto.isActive());
        } catch (Exception ex) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Invalid icon file", ex);
        }
    }

    private String sanitize(String name) {
        return name.replaceAll("[\\r\\n\\\\/]+", "_");
    }
}

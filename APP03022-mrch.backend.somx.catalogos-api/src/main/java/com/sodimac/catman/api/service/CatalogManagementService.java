package com.sodimac.catman.api.service;

import java.util.Optional;

import org.springframework.data.domain.Pageable;

import com.sodimac.catman.api.model.dto.CatalogCreateDto;
import com.sodimac.catman.api.model.dto.CatalogPageResponse;
import com.sodimac.catman.api.model.dto.CatalogResponseDto;
import com.sodimac.catman.api.model.dto.CatalogUpdateDto;


public interface CatalogManagementService {

    CatalogPageResponse findCatalogs(
            Integer id,
            String name,
            String description,
            String catalogType,
            Integer status,
            String code,
            String prefix,
            Pageable pageable);

    Optional<CatalogResponseDto> findById(Integer id);

    CatalogResponseDto createCatalog(CatalogCreateDto createDto, String userId);

    CatalogResponseDto updateCatalog(Integer id, CatalogUpdateDto updateDto, String userId);
}








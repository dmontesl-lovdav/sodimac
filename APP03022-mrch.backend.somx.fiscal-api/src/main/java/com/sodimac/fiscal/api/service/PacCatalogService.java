package com.sodimac.fiscal.api.service;

import com.sodimac.fiscal.api.model.dto.PacCatalogDto;

import java.util.List;
import java.util.Optional;

public interface PacCatalogService {

    List<PacCatalogDto> findAll();
    public List<PacCatalogDto> getPacList();
    Optional<PacCatalogDto> findById(Long id);

}
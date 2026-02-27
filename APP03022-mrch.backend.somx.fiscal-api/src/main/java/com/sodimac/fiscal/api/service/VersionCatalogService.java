package com.sodimac.fiscal.api.service;

import com.sodimac.fiscal.api.model.dto.VersionCatalogDto;

import java.util.List;

public interface VersionCatalogService {

    List<VersionCatalogDto> findAll();

}
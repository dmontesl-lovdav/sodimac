package com.sodimac.fiscal.api.service.impl;

import com.sodimac.fiscal.api.mapper.VersionCatalogMapper;
import com.sodimac.fiscal.api.model.dto.VersionCatalogDto;
import com.sodimac.fiscal.api.model.entity.VersionCatalogEntity;
import com.sodimac.fiscal.api.repository.VersionCatalogRepository;
import com.sodimac.fiscal.api.service.VersionCatalogService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional
public class VersionCatalogServiceImpl implements VersionCatalogService {

    private final VersionCatalogRepository versionCatalogRepository;
    private final VersionCatalogMapper versionCatalogMapper;

    @Override
    @Transactional(readOnly = true)
    public List<VersionCatalogDto> findAll() {
        log.debug("Finding all version catalogs");
        List<VersionCatalogEntity> entities = versionCatalogRepository.findAll();
        return versionCatalogMapper.toDtoList(entities);
    }
}
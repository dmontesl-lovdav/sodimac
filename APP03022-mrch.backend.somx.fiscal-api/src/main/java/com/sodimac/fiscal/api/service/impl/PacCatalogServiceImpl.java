package com.sodimac.fiscal.api.service.impl;

import com.sodimac.fiscal.api.mapper.PacCatalogMapper;
import com.sodimac.fiscal.api.model.dto.PacCatalogDto;
import com.sodimac.fiscal.api.model.entity.PacCatalogEntity;
import com.sodimac.fiscal.api.repository.PacCatalogRepository;
import com.sodimac.fiscal.api.service.PacCatalogService;

import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional
public class PacCatalogServiceImpl implements PacCatalogService {

    private final PacCatalogRepository pacCatalogRepository;
    private final PacCatalogMapper pacCatalogMapper;

    private List<PacCatalogDto> pacList;


    @PostConstruct
    public void init() {
    	setPacList(findAll());
    }


    @Override
    @Transactional(readOnly = true)
    public List<PacCatalogDto> findAll() {
        log.debug("Finding all PAC catalogs");
        List<PacCatalogEntity> entities = pacCatalogRepository.findAll(Sort.by("pacId").ascending());
        return pacCatalogMapper.toDtoList(entities);
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<PacCatalogDto> findById(Long id) {
        log.debug("Finding PAC catalog by id: {}", id);
        return pacCatalogRepository.findById(id)
                .map(pacCatalogMapper::toDto);
    }

	public List<PacCatalogDto> getPacList() {
		return pacList;
	}


	public void setPacList(List<PacCatalogDto> pacList) {
		this.pacList = pacList;
	}
}
package com.sodimac.fiscal.api.service.impl;

import com.sodimac.fiscal.api.mapper.AuthorizedReceiverCatalogMapper;
import com.sodimac.fiscal.api.model.dto.AuthorizedReceiverCatalogDto;
import com.sodimac.fiscal.api.model.entity.AuthorizedReceiverCatalogEntity;
import com.sodimac.fiscal.api.repository.AuthorizedReceiverCatalogRepository;
import com.sodimac.fiscal.api.service.AuthorizedReceiverCatalogService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional
public class AuthorizedReceiverCatalogServiceImpl implements AuthorizedReceiverCatalogService {

    private final AuthorizedReceiverCatalogRepository authorizedReceiverCatalogRepository;
    private final AuthorizedReceiverCatalogMapper authorizedReceiverCatalogMapper;

    @Override
    @Transactional(readOnly = true)
    public List<AuthorizedReceiverCatalogDto> findAll() {
        log.debug("Finding all authorized receiver catalogs");
        List<AuthorizedReceiverCatalogEntity> entities = authorizedReceiverCatalogRepository.findAll();
        return authorizedReceiverCatalogMapper.toDtoList(entities);
    }
}
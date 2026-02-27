package com.sodimac.fiscal.api.service;

import com.sodimac.fiscal.api.model.dto.RelatedCfdiDto;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface RelatedCfdiService {

    Page<RelatedCfdiDto> findAll(Pageable pageable);

}
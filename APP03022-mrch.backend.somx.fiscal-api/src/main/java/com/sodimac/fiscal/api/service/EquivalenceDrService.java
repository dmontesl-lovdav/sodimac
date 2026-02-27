package com.sodimac.fiscal.api.service;

import com.sodimac.fiscal.api.model.dto.EquivalenceDrDto;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface EquivalenceDrService {

    Page<EquivalenceDrDto> findAll(Pageable pageable);

}
package com.sodimac.fiscal.api.service;

import com.sodimac.fiscal.api.model.dto.TotalsDto;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface TotalsService {

    Page<TotalsDto> findAll(Pageable pageable);

}
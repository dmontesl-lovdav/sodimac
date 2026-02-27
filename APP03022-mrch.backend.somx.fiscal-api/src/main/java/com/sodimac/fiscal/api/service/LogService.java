package com.sodimac.fiscal.api.service;

import com.sodimac.fiscal.api.model.dto.LogDto;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface LogService {

    Page<LogDto> findAll(Pageable pageable);
    public LogDto save(LogDto logDto);

}
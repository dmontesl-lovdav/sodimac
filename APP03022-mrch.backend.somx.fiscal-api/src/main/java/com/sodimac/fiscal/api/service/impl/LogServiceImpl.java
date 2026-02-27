package com.sodimac.fiscal.api.service.impl;

import com.sodimac.fiscal.api.mapper.LogMapper;
import com.sodimac.fiscal.api.model.dto.LogDto;
import com.sodimac.fiscal.api.model.entity.LogEntity;
import com.sodimac.fiscal.api.repository.LogRepository;
import com.sodimac.fiscal.api.service.LogService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional
public class LogServiceImpl implements LogService {

    private final LogRepository logRepository;
    private final LogMapper logMapper;
   
    // Paginated methods
    @Override
    @Transactional(readOnly = true)
    public Page<LogDto> findAll(Pageable pageable) {
        log.debug("Finding all logs with pagination: page={}, size={}", pageable.getPageNumber(), pageable.getPageSize());
        Page<LogEntity> entities = logRepository.findAll(pageable);
        return entities.map(logMapper::toDto);
    }
    
    @Override
    public LogDto save(LogDto logDto) {
        //log.debug("Saving LogFiscal Object" + logDto.toString());
        LogEntity logEntity = logMapper.toEntity(logDto);
        LogEntity entity = logRepository.save(logEntity);
        return logMapper.toDto(entity);
    }
}
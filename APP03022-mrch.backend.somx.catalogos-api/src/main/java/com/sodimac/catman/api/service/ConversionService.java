package com.sodimac.catman.api.service;

import com.sodimac.catman.api.model.dto.*;
import org.springframework.data.domain.Pageable;
import java.util.List;

public interface ConversionService {
    ConversionPageResponse search(Integer sourceElementId, Integer targetElementId, String elemento,
                                   String valor, String catalogoOrigen, Integer estatus, Pageable pageable);
    ConversionDto getById(Integer id);
    ConversionDto create(ConversionCreateDto dto, String userId);
    ConversionDto update(Integer id, ConversionUpdateDto dto, String userId);
    ConversionDto setPrincipal(Integer id, Boolean isPrincipal, String userId);
    void delete(Integer id);
    void deleteMultiple(List<Integer> ids);
}


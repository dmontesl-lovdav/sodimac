package com.sodimac.fiscal.api.service;

import com.sodimac.fiscal.api.model.dto.AddendumDto;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface AddendumService {

    Page<AddendumDto> findAll(Pageable pageable);

}
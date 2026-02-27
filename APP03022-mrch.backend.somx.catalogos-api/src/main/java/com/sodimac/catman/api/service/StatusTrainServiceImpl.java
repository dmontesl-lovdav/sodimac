package com.sodimac.catman.api.service;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.sodimac.catman.api.mapper.StatusTrainMapper;
import com.sodimac.catman.api.model.dto.StatusTrainCreateDto;
import com.sodimac.catman.api.model.dto.StatusTrainDto;
import com.sodimac.catman.api.model.dto.StatusTrainListResponse;
import com.sodimac.catman.api.model.dto.StatusTrainUpdateDto;
import com.sodimac.catman.api.model.dto.StatusTrainValidationResponse;
import com.sodimac.catman.api.model.entity.StatusTrain;
import com.sodimac.catman.api.repository.StatusTrainRepository;

/**
 * Implementación del servicio de tren de estatus.
 *
 * @author Sodimac Tech Team
 * @since STM-1166
 */
@Service
@Transactional(readOnly = true)
public class StatusTrainServiceImpl implements StatusTrainService {

    private final StatusTrainRepository repository;

    public StatusTrainServiceImpl(StatusTrainRepository repository) {
        this.repository = repository;
    }

    @Override
    public StatusTrainValidationResponse validateTransition(Integer optionId, Integer sourceStatus, Integer targetStatus) {
        // Primero verificar si el estatus origen existe en alguna transición
        boolean sourceExists = repository.existsByOptionIdAndSourceStatus(optionId, sourceStatus);

        if (!sourceExists) {
            return StatusTrainValidationResponse.sourceNotFound();
        }

        // Buscar la transición específica
        Optional<StatusTrain> transition = repository.findByOptionIdAndSourceStatusAndTargetStatus(
                optionId, sourceStatus, targetStatus);

        if (transition.isEmpty()) {
            return StatusTrainValidationResponse.transitionNotAllowed();
        }

        return StatusTrainValidationResponse.success(StatusTrainMapper.toDto(transition.get()));
    }

    @Override
    public StatusTrainListResponse getAllowedDestinations(Integer optionId, Integer sourceStatus) {
        // Verificar si el estatus origen existe
        boolean sourceExists = repository.existsByOptionIdAndSourceStatus(optionId, sourceStatus);

        if (!sourceExists) {
            return StatusTrainListResponse.sourceNotFound();
        }

        // Obtener todos los destinos permitidos
        List<StatusTrain> transitions = repository.findByOptionIdAndSourceStatus(optionId, sourceStatus);

        List<StatusTrainDto> dtos = transitions.stream()
                .map(StatusTrainMapper::toDto)
                .collect(Collectors.toList());

        return StatusTrainListResponse.success(dtos);
    }

    @Override
    @Transactional
    public StatusTrainDto create(StatusTrainCreateDto createDto) {
        StatusTrain entity = StatusTrainMapper.toEntity(createDto);
        StatusTrain saved = repository.save(entity);
        return StatusTrainMapper.toDto(saved);
    }

    @Override
    @Transactional
    public Optional<StatusTrainDto> update(Integer id, Integer optionId, StatusTrainUpdateDto updateDto) {
        return repository.findById(id)
                .filter(entity -> entity.getOptionId().equals(optionId))
                .map(entity -> {
                    entity.setSourceStatus(updateDto.getSourceStatus());
                    entity.setTargetStatus(updateDto.getTargetStatus());
                    entity.setUpdatedBy(updateDto.getUpdatedBy());
                    StatusTrain saved = repository.save(entity);
                    return StatusTrainMapper.toDto(saved);
                });
    }

    @Override
    @Transactional
    public boolean delete(Integer id) {
        if (repository.existsById(id)) {
            repository.deleteById(id);
            return true;
        }
        return false;
    }

    @Override
    public Optional<StatusTrainDto> findById(Integer id) {
        return repository.findById(id)
                .map(StatusTrainMapper::toDto);
    }
}

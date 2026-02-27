package com.sodimac.fiscal.api.service.impl;

import com.sodimac.fiscal.api.mapper.ReceiverMapper;
import com.sodimac.fiscal.api.model.dto.ReceiverDto;
import com.sodimac.fiscal.api.model.entity.ReceiverEntity;
import com.sodimac.fiscal.api.repository.ReceiverRepository;
import com.sodimac.fiscal.api.service.ReceiverService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional
public class ReceiverServiceImpl implements ReceiverService {

    private final ReceiverRepository receiverRepository;
    private final ReceiverMapper receiverMapper;

    @Override
    @Transactional(readOnly = true)
    public List<ReceiverDto> findAll() {
        log.debug("Finding all receivers");
        List<ReceiverEntity> entities = receiverRepository.findAll();
        return receiverMapper.toDtoList(entities);
    }

    @Override
    public ReceiverEntity getOrCreate(String rfc, String name, String taxRegime) {
        log.debug("Obteniendo o creando receptor con RFC: {}", rfc);

        return receiverRepository.findByRfc(rfc)
                .orElseGet(() -> {
                    log.info("Receptor con RFC {} no existe, creando nuevo registro", rfc);
                    ReceiverEntity newReceiver = new ReceiverEntity();
                    newReceiver.setRfc(rfc);
                    newReceiver.setName(name);
                    newReceiver.setTaxRegime(taxRegime);
                    ReceiverEntity saved = receiverRepository.save(newReceiver);
                    log.info("Receptor creado exitosamente con UUID: {}", saved.getReceiverUuid());
                    return saved;
                });
    }
}
package com.sodimac.fiscal.api.service.impl;

import com.sodimac.fiscal.api.mapper.IssuerMapper;
import com.sodimac.fiscal.api.model.dto.IssuerDto;
import com.sodimac.fiscal.api.model.entity.IssuerEntity;
import com.sodimac.fiscal.api.repository.IssuerRepository;
import com.sodimac.fiscal.api.service.IssuerService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional
public class IssuerServiceImpl implements IssuerService {

    private final IssuerRepository issuerRepository;
    private final IssuerMapper issuerMapper;

    @Override
    @Transactional(readOnly = true)
    public List<IssuerDto> findAll() {
        log.debug("Finding all issuers");
        List<IssuerEntity> entities = issuerRepository.findAll();
        return issuerMapper.toDtoList(entities);
    }

    @Override
    public IssuerEntity getOrCreate(String rfc, String name, String taxRegime) {
        log.debug("Obteniendo o creando emisor con RFC: {}", rfc);

        return issuerRepository.findByRfc(rfc)
                .orElseGet(() -> {
                    log.info("Emisor con RFC {} no existe, creando nuevo registro", rfc);
                    IssuerEntity newIssuer = new IssuerEntity();
                    newIssuer.setRfc(rfc);
                    newIssuer.setName(name);
                    newIssuer.setTaxRegime(taxRegime);
                    IssuerEntity saved = issuerRepository.save(newIssuer);
                    log.info("Emisor creado exitosamente con UUID: {}", saved.getIssuerUuid());
                    return saved;
                });
    }
}
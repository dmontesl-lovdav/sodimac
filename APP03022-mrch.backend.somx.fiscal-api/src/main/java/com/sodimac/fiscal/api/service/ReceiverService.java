package com.sodimac.fiscal.api.service;

import com.sodimac.fiscal.api.model.dto.ReceiverDto;
import com.sodimac.fiscal.api.model.entity.ReceiverEntity;

import java.util.List;

public interface ReceiverService {

    List<ReceiverDto> findAll();

    /**
     * Obtiene un receptor existente por RFC o crea uno nuevo si no existe.
     *
     * @param rfc RFC del receptor
     * @param name Nombre del receptor
     * @param taxRegime Régimen fiscal del receptor
     * @return Receptor encontrado o creado
     */
    ReceiverEntity getOrCreate(String rfc, String name, String taxRegime);

}
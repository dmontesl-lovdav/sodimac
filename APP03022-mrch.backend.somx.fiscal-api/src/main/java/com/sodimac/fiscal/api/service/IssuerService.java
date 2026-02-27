package com.sodimac.fiscal.api.service;

import com.sodimac.fiscal.api.model.dto.IssuerDto;
import com.sodimac.fiscal.api.model.entity.IssuerEntity;

import java.util.List;

public interface IssuerService {

    List<IssuerDto> findAll();

    /**
     * Obtiene un emisor existente por RFC o crea uno nuevo si no existe.
     *
     * @param rfc RFC del emisor
     * @param name Nombre del emisor
     * @param taxRegime Régimen fiscal del emisor
     * @return Emisor encontrado o creado
     */
    IssuerEntity getOrCreate(String rfc, String name, String taxRegime);

}
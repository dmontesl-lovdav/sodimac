package com.sodimac.fiscal.api.service;

import com.sodimac.fiscal.api.model.dto.AuthorizedReceiverCatalogDto;

import java.util.List;

public interface AuthorizedReceiverCatalogService {

    List<AuthorizedReceiverCatalogDto> findAll();

}
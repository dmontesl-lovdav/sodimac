package com.sodimac.rebates.service;

import java.util.List;

import com.sodimac.rebates.dto.CatTipoRebateDto;
import com.sodimac.rebates.model.TipoRebate;

public interface ITipoRebateService {

	List<TipoRebate> getAll();

	List<CatTipoRebateDto> getActive();
	
	CatTipoRebateDto getById(Integer id);

	List<CatTipoRebateDto> getTiposRebatesPerfil(Integer idUser);
}

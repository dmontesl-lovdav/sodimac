package com.sodimac.rebates.service;

import java.util.List;

import com.sodimac.rebates.dto.CatTipoExclusionDto;

public interface ICatTipoExclusionService {

	public List<CatTipoExclusionDto> getCatTipoExclusion();

	public List<CatTipoExclusionDto> getCatTipoExclusionPerfil(Integer idUser);
	
}

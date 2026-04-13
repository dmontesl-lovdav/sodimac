package com.sodimac.rebates.service;

import java.util.List;

import com.sodimac.rebates.dto.CatFlujoEstatusDto;
import com.sodimac.rebates.dto.CatRolDto;
import com.sodimac.rebates.enums.EEvento;

public interface ICatFlujoEstatusService {

	public List<CatFlujoEstatusDto> getCatFlujoEstatus(List<CatRolDto> roles, EEvento eEvento, Integer estatusOrigen);
	
}

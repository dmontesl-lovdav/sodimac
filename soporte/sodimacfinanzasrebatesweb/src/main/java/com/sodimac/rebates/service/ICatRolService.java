package com.sodimac.rebates.service;

import java.util.List;

import com.sodimac.rebates.dto.CatRolDto;

public interface ICatRolService {

	public List<CatRolDto> getRoles(Integer usuario);
	
}

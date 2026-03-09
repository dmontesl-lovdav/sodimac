package com.sodimac.cfdi.service;

import java.util.List;

import com.sodimac.cfdi.entity.fiscal.menu.CatRolEntity;

public interface RolService {
	
	public List<CatRolEntity> findByIdIn(List<Integer> ids);


}

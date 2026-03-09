package com.sodimac.cfdi.service;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.sodimac.cfdi.entity.fiscal.menu.CatRolEntity;
import com.sodimac.cfdi.repository.fiscal.menu.CatRolRepository;



@Service
public class RolServiceImpl implements RolService {
	
	@Autowired
	private CatRolRepository catRolRepository;

	@Override
	public List<CatRolEntity> findByIdIn(List<Integer> ids) {
		// TODO Auto-generated method stub
		List<CatRolEntity> roles = catRolRepository.findByIdIn(ids);
		return roles;
	}

}

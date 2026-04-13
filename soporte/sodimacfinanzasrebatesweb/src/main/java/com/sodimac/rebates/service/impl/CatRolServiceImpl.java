package com.sodimac.rebates.service.impl;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.sodimac.rebates.dto.CatRolDto;
import com.sodimac.rebates.mapper.CatRolMapper;
import com.sodimac.rebates.model.entity.CatRolEntity;
import com.sodimac.rebates.repository.CatRolRepository;
import com.sodimac.rebates.service.ICatRolService;

@Service
public class CatRolServiceImpl implements ICatRolService {

	@Autowired
	private CatRolRepository catRolRepository;
	
	@Override
	public List<CatRolDto> getRoles(Integer usuario) {
		List<CatRolEntity> entities = this.catRolRepository.findRolesByUser(usuario);
		return CatRolMapper.convertToDtos(entities);
	}

}

package com.sodimac.rebates.service;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.sodimac.rebates.model.entity.CatPerfilEntity;
import com.sodimac.rebates.repository.CatPerfilRepository;

@Service
public class CatPerfilService implements ICatPerfilService {

	@Autowired
	CatPerfilRepository catPerfilRepository;

	public List<CatPerfilEntity> getPerfiles() {
		return catPerfilRepository.findAll();
	}

}

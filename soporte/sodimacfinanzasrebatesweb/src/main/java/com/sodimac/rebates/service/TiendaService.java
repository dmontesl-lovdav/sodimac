package com.sodimac.rebates.service;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.sodimac.rebates.model.CatTiendaEntity;
import com.sodimac.rebates.repository.CatTiendaRepository;

@Service
public class TiendaService implements ITiendaService {

	@Autowired
	private CatTiendaRepository catTiendaRepository;
	
	@Override
	public List<CatTiendaEntity> getTiendas() {
		return this.catTiendaRepository.findByActivo(true);
	}

}

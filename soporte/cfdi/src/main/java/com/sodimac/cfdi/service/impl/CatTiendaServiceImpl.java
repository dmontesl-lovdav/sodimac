package com.sodimac.cfdi.service.impl;

import java.util.ArrayList;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Isolation;
import org.springframework.transaction.annotation.Transactional;

import com.google.gson.Gson;
import com.sodimac.cfdi.entity.fiscal.CatTiendaEntity;
import com.sodimac.cfdi.models.CatTiendaModel;
import com.sodimac.cfdi.models.EstatusPagoModelItem;
import com.sodimac.cfdi.repository.fiscal.CatTiendaRepository;
import com.sodimac.cfdi.service.CatTiendaService;

@Service
public class CatTiendaServiceImpl implements CatTiendaService {

	@Autowired
	private CatTiendaRepository catTiendaRepository;
	
	@Override
	public List<CatTiendaModel> getTiendas() {
		List<CatTiendaModel> listTiendas = new ArrayList<CatTiendaModel>(0);
		List<CatTiendaEntity> listEntity = this.catTiendaRepository.findByActivo(true);
		if (listEntity != null) {
			for (CatTiendaEntity entity : listEntity) {
				CatTiendaModel model = this.getModel(entity);
				listTiendas.add(model);
			}
		}
		return listTiendas;
	}
	
	@Transactional(isolation = Isolation.READ_UNCOMMITTED)
	@Override
	public String getGsonTiendas() {
		
		List<CatTiendaModel> listTiendas = this.getTiendas();
		
		Gson gson= new Gson();
		String resultado = gson.toJson(listTiendas);
		return resultado;
	}
	
	private CatTiendaModel getModel(CatTiendaEntity entity) {
		CatTiendaModel model = new CatTiendaModel();
		model.setId( entity.getId() );
		model.setDescripcion( entity.getDescripcion() );
		model.setNombre( entity.getNombre() );
		model.setId( entity.getId() );
		return model;
	}
}

package com.sodimac.cfdi.service;

import java.util.List;

import com.sodimac.cfdi.models.CatTiendaModel;

public interface CatTiendaService {

	public List<CatTiendaModel> getTiendas();

	public String getGsonTiendas();
	
}

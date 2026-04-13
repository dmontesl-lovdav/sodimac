package com.sodimac.rebates.service;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.sodimac.rebates.enums.EEstatus;
import com.sodimac.rebates.model.CatTipoOrdenCompraEntity;
import com.sodimac.rebates.repository.CatTipoOrdenCompraRepository;

@Service
public class TipoOrdenCompraService implements ITipoOrdenCompraService {

	@Autowired
	private CatTipoOrdenCompraRepository catTipoOrdenCompraRepository;
	
	@Override
	public List<CatTipoOrdenCompraEntity> getTiposOrdenCompra() {
		return this.catTipoOrdenCompraRepository.findByEstatusOrderByDescripcion(EEstatus.ACTIVO.getId());
	}

}

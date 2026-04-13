package com.sodimac.rebates.service;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.sodimac.rebates.enums.EEstatus;
import com.sodimac.rebates.model.CatEstadoOrdenCompraEntity;
import com.sodimac.rebates.repository.CatEstadoOrdenCompraRepository;

@Service
public class EstadoOrdenCompraService implements IEstadoOrdenCompraService {
	
	@Autowired
	private CatEstadoOrdenCompraRepository catEstadoOrdenCompraRepository;

	@Override
	public List<CatEstadoOrdenCompraEntity> getEstadosOrdenCompra() {
		return this.catEstadoOrdenCompraRepository.findByEstatus(EEstatus.ACTIVO.getId());
	}

}

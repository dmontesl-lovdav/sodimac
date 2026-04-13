package com.sodimac.rebates.service;

import java.util.List;

import com.sodimac.rebates.model.RebateOrdenCompraEntity;
import com.sodimac.rebates.model.ReporteOrdenCompra;

public interface IRebateOrdenCompraService {

	public List<RebateOrdenCompraEntity> getOrdenCompra(ReporteOrdenCompra ordenCompra);
	
}

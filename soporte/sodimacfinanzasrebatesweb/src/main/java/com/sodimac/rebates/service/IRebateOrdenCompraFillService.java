package com.sodimac.rebates.service;

import java.util.List;

import com.sodimac.rebates.model.RebateOrdenCompraFillEntity;
import com.sodimac.rebates.model.ReporteOrdenCompraFill;

public interface IRebateOrdenCompraFillService {

	public List<RebateOrdenCompraFillEntity> getOrdenCompraFill(ReporteOrdenCompraFill ordenCompraFill);
	
}

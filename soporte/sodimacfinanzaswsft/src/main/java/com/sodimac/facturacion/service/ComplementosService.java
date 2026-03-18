package com.sodimac.facturacion.service;

import java.util.List;

import com.sodimac.facturacion.models.CompTemporalModel;
import com.sodimac.facturacion.models.FacturasCompModel;

public interface ComplementosService {
	
	public int timbrarTipoProceso(CompTemporalModel model, int tipoProceso);
	
	public List<FacturasCompModel> getFacturas(int idTransaccionPago);
	
	void actualizarEstatusLog(CompTemporalModel model, int estatus);
}

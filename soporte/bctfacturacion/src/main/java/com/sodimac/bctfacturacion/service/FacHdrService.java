package com.sodimac.bctfacturacion.service;

import java.util.Date;
import java.util.List;

import com.sodimac.bctfacturacion.model.FacturacionGlobalVentaModel;

public interface FacHdrService {
	
	public List<FacturacionGlobalVentaModel> getTimbradoGlobalVenta(Date pFechaInicio,Date pFechaFinal);
	
	public List<FacturacionGlobalVentaModel> getTimbradoGlobalDevolucionD(Date pFechaInicio,Date pFechaFinal);
	
	public List<FacturacionGlobalVentaModel> getTimbradoGlobalNotaCredito(Date pFechaInicio,Date pFechaFinal);
	
	public List<FacturacionGlobalVentaModel> getTimbradoGlobalDevolucionFDC(Date pFechaInicio,Date pFechaFinal);
}

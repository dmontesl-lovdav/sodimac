package com.sodimac.cfdi.service;

import java.util.List;

import com.sodimac.cfdi.models.FolioFacturaModel;

public interface FolioFacturaService2 {

	public Integer obtenerIdFolioFactura(Integer pFolioFactura);
	
	public Double obtenerTotalFolioFactura(Integer pIdFolioFactura);
	
	public Double obtenerTotalPagosFolioFactura(Integer pIdFolioFactura);
	
	public Double obtenerTotalOtrosPagosFolioFactura(Integer pIdFolioFactura, Integer pIdPagoComplemento);
	
	public List<FolioFacturaModel> obtenerFacturasByIdFolioFactura(Integer pIdFolioFactura);
	
}

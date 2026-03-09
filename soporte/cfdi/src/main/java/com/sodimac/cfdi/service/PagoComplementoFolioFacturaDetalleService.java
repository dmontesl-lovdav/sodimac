package com.sodimac.cfdi.service;

import java.util.List;

import com.sodimac.cfdi.models.PagoComplementoFolioFacturaDetalleModel;

public interface PagoComplementoFolioFacturaDetalleService {

	public List<PagoComplementoFolioFacturaDetalleModel> obtenerFacturasByIdPagoComplementoFolioFactura(Integer pIdPagoComplementoFolioFactura);
	
	public List<PagoComplementoFolioFacturaDetalleModel> obtenerFacturasByIdFactura(Integer pIdFactura);

	public void registrarPagoComplementoFolioFacturaDetalle(Integer idPagoComplementoFolioFactura,
			List<PagoComplementoFolioFacturaDetalleModel> pagosComplementoFolioFacturaDet);
}

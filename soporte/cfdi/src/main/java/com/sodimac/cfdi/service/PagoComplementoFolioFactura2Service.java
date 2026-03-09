package com.sodimac.cfdi.service;

import java.util.List;

import com.sodimac.cfdi.models.PagoComplementoFolioFacturaModel;

public interface PagoComplementoFolioFactura2Service {
	
	public Integer obtenerIdPagoComplementoFolioFacturaByIdPagoComplemento(Integer pIdPagoComplemento);
	
	public Integer obtenerIdFolioFacturaByIdPagoComplemento(Integer pIdPagoComplemento);
	
	public PagoComplementoFolioFacturaModel obtenerPagoComplementoByIdPagoComplementoFolioFactura(Integer pIdPagoComplementoFolioFactura);

	public List<PagoComplementoFolioFacturaModel> obtenerPagoComplementoByIdFolioFactura(Integer pIdFolioFactura);

	public void registrarPagoComplementoFolioFactura(PagoComplementoFolioFacturaModel modelFolio);

	public void actualizarSaldosPagoComplemento(Integer idPagoComplemento, Double totalFacturas, Double saldoPendiente, String rfc);
	
	public void desvincularPagoComplemento(Integer idPagoComplemento);
}

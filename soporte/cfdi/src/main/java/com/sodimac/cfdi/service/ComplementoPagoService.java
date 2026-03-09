package com.sodimac.cfdi.service;

import com.sodimac.cfdi.entity.fiscal.PagoComplementoEntity;
import com.sodimac.cfdi.models.PagoComplementoDetalleModel;
import com.sodimac.cfdi.util.enums.EComplementoPago;

public interface ComplementoPagoService {

	public PagoComplementoDetalleModel getDetalleComplementoPagoPorFolio(Integer valueOf, Integer valueOf2, boolean busqueda, EComplementoPago eComplementoPago);
	
	public void asignarComplementoPagoFolio(Integer idPagoComplemento, Integer folioFactura, EComplementoPago eComplementoPago);
	
	public void actualizarEstatusTimbrado(Integer idPagoComplemento, String estatus);
	
	public void actualizarSaldosPagoComplemento(Integer idPagoComplemento, PagoComplementoDetalleModel model);
	
	public void actualizarSaldosPagoComplemento(Integer idPagoComplemento, Double totalFacturas, Double saldoPendiente, String rfc);
	
	public void desvincularPagoComplemento(Integer idPagoComplemento);

	public PagoComplementoEntity getPagoComplemento(Integer idPagoComplemento);
	
}

package com.sodimac.cfdi.service.impl;

import java.util.ArrayList;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.sodimac.cfdi.models.PagoComplementoFolioFacturaDetalleModel;
import com.sodimac.cfdi.models.PagoComplementoFolioFacturaModel;
import com.sodimac.cfdi.repository.fiscal.PagoComplementoFolioFacturaRepository;
import com.sodimac.cfdi.service.PagoComplementoFolioFactura2Service;
import com.sodimac.cfdi.service.PagoComplementoFolioFacturaDetalleService;

@Service
public class PagoComplementoFolioFacturaServiceImpl implements PagoComplementoFolioFactura2Service {

	@Autowired
	private PagoComplementoFolioFacturaRepository complementoFolioFactura2Repository;
	
	@Autowired
	private PagoComplementoFolioFacturaDetalleService complementoFolioFacturaDetalleService;
	
	@Override
	public Integer obtenerIdPagoComplementoFolioFacturaByIdPagoComplemento(Integer pIdPagoComplemento) {
		return this.complementoFolioFactura2Repository.getIdPagoComplementoFolioFacturaByIdPago(pIdPagoComplemento);
	}
	
	@Override
	public Integer obtenerIdFolioFacturaByIdPagoComplemento(Integer pIdPagoComplemento) {
		return this.complementoFolioFactura2Repository.getIdFolioFacturaByIdPagoComplemento(pIdPagoComplemento);
	}
	
	@Override
	public PagoComplementoFolioFacturaModel obtenerPagoComplementoByIdPagoComplementoFolioFactura(
			Integer pIdPagoComplementoFolioFactura) {
		PagoComplementoFolioFacturaModel pago = null;
		List<Object[]> listPagosComplemento = this.complementoFolioFactura2Repository.getPagoComplementoByIdPagoComplementoFolioFactura(pIdPagoComplementoFolioFactura);
		if (listPagosComplemento != null && listPagosComplemento.size() > 0) {
			Object[] arrPago = listPagosComplemento.get(0);
			pago = this.getPagoByArray(arrPago);
		}
		return pago;
	}
	
	@Override
	public List<PagoComplementoFolioFacturaModel> obtenerPagoComplementoByIdFolioFactura(Integer pIdFolioFactura) {
		List<PagoComplementoFolioFacturaModel> listPagosComplementoFolioFac = new ArrayList<PagoComplementoFolioFacturaModel>();
		List<Object[]> listPagosComplemento = this.complementoFolioFactura2Repository.getPagoComplementoByIdFolioFactura(pIdFolioFactura);
		if (listPagosComplemento != null) {
			for (Object[] arrPago : listPagosComplemento) {
				Integer idPagoComplementoFolioFactura = Integer.valueOf(arrPago[0].toString());
				PagoComplementoFolioFacturaModel pago = this.getPagoByArray(arrPago);
				List<PagoComplementoFolioFacturaDetalleModel> pagosComplementoFolioFacturaDet = 
						complementoFolioFacturaDetalleService.obtenerFacturasByIdPagoComplementoFolioFactura(idPagoComplementoFolioFactura);
				pago.setPagosComplementoFolioFacturaDet(pagosComplementoFolioFacturaDet);
				
				listPagosComplementoFolioFac.add(pago);
			}
		}
		return listPagosComplementoFolioFac;
	}

	@Override
	public void registrarPagoComplementoFolioFactura(PagoComplementoFolioFacturaModel modelFolio) {
		this.complementoFolioFactura2Repository.registrarPagoComplementoFolioFactura(modelFolio.getIdPagoComplemento()
				, modelFolio.getIdFolioFactura()
				, modelFolio.getTotalFolioFactura()
				, modelFolio.getMontoPago());
		
		Integer idPagoComplementoFolioFactura = this.obtenerIdPagoComplementoFolioFacturaByIdPagoComplemento( modelFolio.getIdPagoComplemento() );
		this.complementoFolioFacturaDetalleService.registrarPagoComplementoFolioFacturaDetalle(idPagoComplementoFolioFactura, modelFolio.getPagosComplementoFolioFacturaDet());
	}
	
	@Override
	public void actualizarSaldosPagoComplemento(Integer idPagoComplemento, Double totalFacturas, Double saldoPendiente, String rfc) {
		this.complementoFolioFactura2Repository.actualizarSaldosPagoComplemento(idPagoComplemento, totalFacturas, saldoPendiente, rfc);
	}
	
	@Override
	public void desvincularPagoComplemento(Integer idPagoComplemento) {
		this.complementoFolioFactura2Repository.desvincularPagoComplemento(idPagoComplemento);
	}

	private PagoComplementoFolioFacturaModel getPagoByArray(Object[] arrPago) {
		
		PagoComplementoFolioFacturaModel pago = new PagoComplementoFolioFacturaModel();
		
		Integer idPagoComplementoFolioFactura = Integer.valueOf(arrPago[0].toString());
		pago.setIdPagoComplementoFolioFactura( idPagoComplementoFolioFactura );
		pago.setIdPagoComplemento( Integer.valueOf(arrPago[1].toString()) );
		pago.setIdFolioFactura(Integer.valueOf(arrPago[2].toString()));
		pago.setTotalFolioFactura(Double.valueOf(arrPago[3].toString()));
		pago.setMontoPago( Double.valueOf(arrPago[4].toString()) );
		
		return pago;
	}
}

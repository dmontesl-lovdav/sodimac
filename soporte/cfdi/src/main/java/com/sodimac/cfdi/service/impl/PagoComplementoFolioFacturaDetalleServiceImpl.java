package com.sodimac.cfdi.service.impl;

import java.util.ArrayList;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.sodimac.cfdi.models.PagoComplementoFolioFacturaDetalleModel;
import com.sodimac.cfdi.models.PagoComplementoFolioFacturaModel;
import com.sodimac.cfdi.repository.fiscal.PagoComplementoFolioFacturaDetalleRepository;
import com.sodimac.cfdi.service.PagoComplementoFolioFactura2Service;
import com.sodimac.cfdi.service.PagoComplementoFolioFacturaDetalleService;
import com.sodimac.cfdi.util.enums.EFacturaComplemento;

@Service
public class PagoComplementoFolioFacturaDetalleServiceImpl implements PagoComplementoFolioFacturaDetalleService {

	@Autowired
	private PagoComplementoFolioFacturaDetalleRepository pagoComplementoFolioFacturaDetalleRepository;
	
	@Autowired
	private PagoComplementoFolioFactura2Service complementoFolioFactura2Service;
	
	@Override
	public List<PagoComplementoFolioFacturaDetalleModel> obtenerFacturasByIdPagoComplementoFolioFactura(
			Integer pIdPagoComplementoFolioFactura) {
		List<Object[]> listFacturasArr = this.pagoComplementoFolioFacturaDetalleRepository.getFacturasByIdPagoComplementoFolioFactura(pIdPagoComplementoFolioFactura);
		List<PagoComplementoFolioFacturaDetalleModel> listFacturas = this.getFacturas(listFacturasArr, true);
		return listFacturas;
	}

	@Override
	public List<PagoComplementoFolioFacturaDetalleModel> obtenerFacturasByIdFactura(Integer pIdFactura) {
		List<Object[]> listFacturasArr = this.pagoComplementoFolioFacturaDetalleRepository.getFacturasByIdFactura(pIdFactura);
		List<PagoComplementoFolioFacturaDetalleModel> listFacturas = this.getFacturas(listFacturasArr, false);
		return listFacturas;
	}
	
	@Override
	public void registrarPagoComplementoFolioFacturaDetalle(Integer idPagoComplementoFolioFactura,
			List<PagoComplementoFolioFacturaDetalleModel> pagosComplementoFolioFacturaDet) {
		if (pagosComplementoFolioFacturaDet != null) {
			for (PagoComplementoFolioFacturaDetalleModel pagoCompDet : pagosComplementoFolioFacturaDet) {
				
				if (!pagoCompDet.isPersistente()) {
					this.pagoComplementoFolioFacturaDetalleRepository.registrarPagoComplementoFolioFacturaDetalle(idPagoComplementoFolioFactura
							, pagoCompDet.getIdFactura()
							, pagoCompDet.getParcialidad()
							, pagoCompDet.getEquivalencia()
							, pagoCompDet.getMonedaPago()
							, pagoCompDet.getMontoRealFactura()
							, pagoCompDet.getMontoPorPagar()
							, pagoCompDet.getMontoTotalNC()
							, pagoCompDet.getImporteSaldoAnterior()
							, pagoCompDet.getImportePagado()
							, pagoCompDet.getImportePosterior()
							, pagoCompDet.getEstatusFactura().getIdEstatus());
				}
			}
		}
	}
	
	private List<PagoComplementoFolioFacturaDetalleModel> getFacturas(List<Object[]> listFacturasArr, boolean recursivo) {
		List<PagoComplementoFolioFacturaDetalleModel> listFacturas = new ArrayList<PagoComplementoFolioFacturaDetalleModel>();
		if (listFacturasArr != null) {
			for (Object[] arrObj : listFacturasArr) {
				PagoComplementoFolioFacturaDetalleModel factura = new PagoComplementoFolioFacturaDetalleModel();
				
				Integer idPagoComplementofolioFactura = Integer.valueOf(arrObj[1].toString());
				Integer idFactura = Integer.valueOf(arrObj[2].toString());
				String estatusFactura = arrObj[10].toString();
				EFacturaComplemento estatus = EFacturaComplemento.getFacturaComplemento(Integer.parseInt(estatusFactura));
				
				factura.setIdPagoComplementoFolioFacturaDet( Integer.valueOf(arrObj[0].toString()) );
				factura.setIdPagoComplementoFolioFactura( idPagoComplementofolioFactura );
				factura.setIdFactura(idFactura);
				factura.setParcialidad(Integer.valueOf(arrObj[3].toString()));
				factura.setEquivalencia(Double.valueOf(arrObj[4].toString()));
				factura.setMonedaPago(arrObj[5].toString());
				factura.setMontoPorPagar(Double.valueOf(arrObj[6].toString()));
				factura.setImporteSaldoAnterior(Double.valueOf(arrObj[7].toString()));
				factura.setImportePagado(Double.valueOf(arrObj[8].toString()));
				factura.setImportePosterior(Double.valueOf(arrObj[9].toString()));
				factura.setMontoTotalNC(Double.valueOf(arrObj[11].toString()));
				factura.setMontoRealFactura(Double.valueOf(arrObj[12].toString()));
				factura.setOrden( (arrObj[13] != null ? Integer.valueOf(arrObj[13].toString()) : null ) );
				factura.setEstatusFactura(estatus);
				factura.setPersistente(true);
				
				PagoComplementoFolioFacturaModel pagoComplementoFolioFactura = 
						complementoFolioFactura2Service.obtenerPagoComplementoByIdPagoComplementoFolioFactura(idPagoComplementofolioFactura);
				factura.setPagoComplementoFolioFactura(pagoComplementoFolioFactura);
				
				if (recursivo) {
					List<PagoComplementoFolioFacturaDetalleModel> facturasPagosRelacionados = 
							this.obtenerFacturasByIdFactura(idFactura);
					
					factura.setFacturasPagosRelacionados(facturasPagosRelacionados);
				}
				listFacturas.add(factura);
			}
		}
		return listFacturas;
	}
	
}

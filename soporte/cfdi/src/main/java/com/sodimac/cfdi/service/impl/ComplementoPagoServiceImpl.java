package com.sodimac.cfdi.service.impl;
import java.math.BigDecimal;
import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Optional;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.sodimac.cfdi.entity.fiscal.PagoComplementoEntity;
import com.sodimac.cfdi.models.FolioFacturaModel;
import com.sodimac.cfdi.models.PagoComplementoDetalleModel;
import com.sodimac.cfdi.models.PagoComplementoFolioFacturaDetalleModel;
import com.sodimac.cfdi.models.PagoComplementoFolioFacturaViewModel;
import com.sodimac.cfdi.models.PagoComplementoFolioFacturaModel;
import com.sodimac.cfdi.repository.fiscal.ComplementoPagoRepository;
import com.sodimac.cfdi.service.CatConfiguracionService;
import com.sodimac.cfdi.service.ComplementoPagoService;
import com.sodimac.cfdi.service.FolioFacturaService2;
import com.sodimac.cfdi.service.PagoComplementoFolioFactura2Service;
import com.sodimac.cfdi.service.PagoComplementoFolioFacturaDetalleService;
import com.sodimac.cfdi.service.SeguridadService;
import com.sodimac.cfdi.util.enums.EComplementoPago;
import com.sodimac.cfdi.util.enums.EFacturaComplemento;

@Service
public class ComplementoPagoServiceImpl implements ComplementoPagoService {

	private Logger logger = LoggerFactory.getLogger(ComplementoPagoServiceImpl.class);

	private NumberFormat df = NumberFormat.getCurrencyInstance(new Locale("en", "US"));
	
	private static final String MONEDA_MX = "MXN";
	
	@Autowired
	private ComplementoPagoRepository complementoPagoRepository;
	
	@Autowired
	private FolioFacturaService2 facturaService2;
	
	@Autowired
	private PagoComplementoFolioFactura2Service pagoComplementoFolioFactura2Service;
	
	@Autowired
	private PagoComplementoFolioFacturaDetalleService complementoFolioFacturaDetalleService;
	
	@Autowired
	private SeguridadService seguridadService;
	
	@Autowired
	private CatConfiguracionService catConfiguracionService;

	@Override
	public PagoComplementoDetalleModel getDetalleComplementoPagoPorFolio(Integer idPagoComplemento, Integer folioFactura
			, boolean busqueda, EComplementoPago eComplementoPago) {
		
		PagoComplementoDetalleModel model = new PagoComplementoDetalleModel();
		model.setMsgValidacion(null);
		model.setBtnVincularPago(false);
		model.setBtnAsignarComplemento(false);
		model.setValidaComplemento(false);
		
		BigDecimal totalFolio = BigDecimal.ZERO;
		BigDecimal totalOtrosPagos = BigDecimal.ZERO;
		BigDecimal importePago = BigDecimal.ZERO;
		BigDecimal totalRemanenteFolioFactura = BigDecimal.ZERO;
		PagoComplementoEntity pagoComplementoEntity = null;
		Integer idFolioFactura = null;
		
		if (eComplementoPago.equals(EComplementoPago.TIMBRADO)) {
			busqueda = false;
		}
		
		pagoComplementoEntity = this.getPagoComplemento(idPagoComplemento);
		if (pagoComplementoEntity != null && pagoComplementoEntity.getImporte() != null) {
			importePago = BigDecimal.valueOf( pagoComplementoEntity.getImporte().doubleValue() );
		}
		
		//Si es busqueda es por que aun no se tiene un folio asociado al pago
		if (busqueda) {
			idFolioFactura = this.facturaService2.obtenerIdFolioFactura(folioFactura);
			if (idFolioFactura != null) {
				totalFolio = BigDecimal.valueOf(this.getValue(this.facturaService2.obtenerTotalFolioFactura(idFolioFactura)));
				totalOtrosPagos = BigDecimal.valueOf(this.getValue(this.facturaService2.obtenerTotalOtrosPagosFolioFactura(idFolioFactura, idPagoComplemento)));
			}
			
			if (idFolioFactura == null) {
				model.setMsgValidacion("No existe el folio factura");
				model.setBtnAsignarComplemento(false);
				model.setValidaComplemento(false);
			} else {
				
				boolean montoInValido = false;
				totalRemanenteFolioFactura = totalFolio.subtract(totalOtrosPagos).subtract(importePago);
				if (totalRemanenteFolioFactura.doubleValue() < 0.0) {
					String rangoValido = this.catConfiguracionService.findParameterByKey("Aplicacion.ToleranciaTicket");
					BigDecimal bRangoValido = new BigDecimal(rangoValido);
					montoInValido = !( totalRemanenteFolioFactura.abs().compareTo(bRangoValido) < 0 );
				}
				
				//if (totalRemanenteFolioFactura.doubleValue() < 0.0) {
				if (  montoInValido ) {
					model.setMsgValidacion("El total de pago es mayor al saldo disponible");
					model.setBtnAsignarComplemento(false);
					model.setValidaComplemento(false);
					
					List<PagoComplementoFolioFacturaViewModel> listComplementoFolioFactura = new ArrayList<>();
					List<FolioFacturaModel> listFoliosFactura = this.facturaService2.obtenerFacturasByIdFolioFactura(idFolioFactura);
					List<PagoComplementoFolioFacturaModel> listPagoComplementos = this.pagoComplementoFolioFactura2Service.obtenerPagoComplementoByIdFolioFactura(idFolioFactura);
					
					//Al existir el idFoliFactura, hay al menos una factura asociada a este folio
					FolioFacturaModel folioFacturaModel = listFoliosFactura.get(0);
					int index = 0;
					for (PagoComplementoFolioFacturaModel pagoComplemento : listPagoComplementos) {
						totalFolio = BigDecimal.valueOf(pagoComplemento.getTotalFolioFactura());
						
						for (int i=0; i<pagoComplemento.getPagosComplementoFolioFacturaDet().size(); i++) {
							PagoComplementoFolioFacturaDetalleModel comFolioDetalle = pagoComplemento.getPagosComplementoFolioFacturaDet().get(i);
							PagoComplementoFolioFacturaViewModel complementoFacturaModel = this.getComplementoFacturaDetModel(folioFacturaModel, comFolioDetalle, index);
							complementoFacturaModel.setColorPagoComplemento(false);
							
							model.setRazonSocial( complementoFacturaModel.getRazonSocial() );
							model.setRfc( complementoFacturaModel.getRfc() );
							listComplementoFolioFactura.add(complementoFacturaModel);
							index ++;
						}
					}
					model.setListPagosFoliosFacturaView(listComplementoFolioFactura);
					
				} else {
					model.setMsgValidacion(null);
					model.setBtnVincularPago(false);
					model.setBtnAsignarComplemento(true);
					model.setValidaComplemento(true);
				}
			}//if (idFolioFactura == null) {
			
			if (model.isValidaComplemento()) {
				List<FolioFacturaModel> listFoliosFactura = this.facturaService2.obtenerFacturasByIdFolioFactura(idFolioFactura);
				List<PagoComplementoFolioFacturaModel> listPagoComplementos = this.pagoComplementoFolioFactura2Service.obtenerPagoComplementoByIdFolioFactura(idFolioFactura);
				
				List<PagoComplementoFolioFacturaModel> listPagoComplementosRegistrar = this.merge(listFoliosFactura
														, listPagoComplementos
														, idFolioFactura
														, totalFolio.doubleValue()
														, pagoComplementoEntity);
				
				List<PagoComplementoFolioFacturaViewModel> listComplementoFolioFactura = new ArrayList<>();
				for (PagoComplementoFolioFacturaModel pagoComplemento : listPagoComplementosRegistrar) {
					
					for (int i=0; i<pagoComplemento.getPagosComplementoFolioFacturaDet().size(); i++) {
						PagoComplementoFolioFacturaDetalleModel comFolioDetalle = pagoComplemento.getPagosComplementoFolioFacturaDet().get(i);
						
						boolean registroNuevo = comFolioDetalle.isColorPagoComplemento();
						PagoComplementoFolioFacturaModel pagoComplementoOrigen = null;
						if (registroNuevo) {
							pagoComplementoOrigen = pagoComplemento;
							pagoComplementoOrigen.setIdPagoComplemento(idPagoComplemento);
							
						} else {
							pagoComplementoOrigen = comFolioDetalle.getPagoComplementoFolioFactura();
						}
						pagoComplementoOrigen.setFolioFactura( pagoComplemento.getFolioFactura() );
						
						PagoComplementoFolioFacturaViewModel complementoFacturaModel = this.getComplementoFacturaInitModel(pagoComplementoOrigen, comFolioDetalle, i);
						complementoFacturaModel.setColorPagoComplemento(registroNuevo);
						
						model.setRazonSocial( complementoFacturaModel.getRazonSocial() );
						model.setRfc( complementoFacturaModel.getRfc() );
						
						listComplementoFolioFactura.add(complementoFacturaModel);
					}
				}
				model.setListPagosFoliosFacturaView(listComplementoFolioFactura);
				model.setListPagoComplementos(listPagoComplementosRegistrar);
			}
			
		} else {//if (busqueda)
			model.setValidaComplemento(true);
			idFolioFactura = this.pagoComplementoFolioFactura2Service.obtenerIdFolioFacturaByIdPagoComplemento(idPagoComplemento);
			idFolioFactura = (idFolioFactura != null) ? idFolioFactura : 0;
			
			List<PagoComplementoFolioFacturaViewModel> listComplementoFolioFactura = new ArrayList<>();
			
			if (idFolioFactura > 0) {
				model.setExistePagoComplemento(true);
				model.setBtnVincularPago(true);
				model.setBtnAsignarComplemento(false);
				model.setMsgValidacion(null);
				model.setValidaComplemento(true);
				
				if (eComplementoPago.equals(EComplementoPago.TIMBRADO)) {
					model.setBtnVincularPago(false);
				} 
				
				
				totalFolio = BigDecimal.valueOf(this.getValue(this.facturaService2.obtenerTotalFolioFactura(idFolioFactura)));
				totalOtrosPagos = BigDecimal.valueOf(this.getValue(this.facturaService2.obtenerTotalOtrosPagosFolioFactura(idFolioFactura, idPagoComplemento)));
				
				
				List<FolioFacturaModel> listFoliosFactura = this.facturaService2.obtenerFacturasByIdFolioFactura(idFolioFactura);
				List<PagoComplementoFolioFacturaModel> listPagoComplementos = this.pagoComplementoFolioFactura2Service.obtenerPagoComplementoByIdFolioFactura(idFolioFactura);
				
				//Al existir el idFoliFactura, hay al menos una factura asociada a este folio
				FolioFacturaModel folioFacturaModel = listFoliosFactura.get(0);
				int index = 0;
				for (PagoComplementoFolioFacturaModel pagoComplemento : listPagoComplementos) {
					
					if (pagoComplemento.getPagosComplementoFolioFacturaDet() != null && 
							pagoComplemento.getPagosComplementoFolioFacturaDet().size() > 0) {
						for (int i=0; i<pagoComplemento.getPagosComplementoFolioFacturaDet().size(); i++) {
							PagoComplementoFolioFacturaDetalleModel comFolioDetalle = pagoComplemento.getPagosComplementoFolioFacturaDet().get(i);
							PagoComplementoFolioFacturaViewModel complementoFacturaModel = this.getComplementoFacturaDetModel(folioFacturaModel, comFolioDetalle, index);
							
							if (idPagoComplemento.intValue() == comFolioDetalle.getPagoComplementoFolioFactura().getIdPagoComplemento().intValue()) {
								complementoFacturaModel.setColorPagoComplemento(true);
							}
							
							model.setRazonSocial( complementoFacturaModel.getRazonSocial() );
							model.setRfc( complementoFacturaModel.getRfc() );
							listComplementoFolioFactura.add(complementoFacturaModel);
							
							index ++;
						}
						
						//Si el pago se encuentra en proceso solo se puede desvincular si idPagoComplemento es el último registrado
						if (eComplementoPago.equals(EComplementoPago.COMPLEMENTO_EN_PROCESO)) {
							PagoComplementoFolioFacturaDetalleModel comFolioDetalle = pagoComplemento.getPagosComplementoFolioFacturaDet().get( pagoComplemento.getPagosComplementoFolioFacturaDet().size() - 1 );
							if (idPagoComplemento.intValue() == comFolioDetalle.getPagoComplementoFolioFactura().getIdPagoComplemento().intValue()) {
								model.setBtnVincularPago(true);
								model.setValidaComplemento(true);
								model.setMsgValidacion(null);
							} else {
								model.setBtnVincularPago(false);
								model.setValidaComplemento(false);
								model.setMsgValidacion("Para desvincular es necesario desvincular el último pago");
							}
						}
					}
				}
				totalRemanenteFolioFactura = totalFolio.subtract(totalOtrosPagos).subtract(importePago);
			} //if (idFolioFactura > 0)
			
			model.setListPagosFoliosFacturaView(listComplementoFolioFactura);
		}
		
		if (model.getListPagosFoliosFacturaView() != null && 
			model.getListPagosFoliosFacturaView().size() > 0) {
			model.setFolioFactura( model.getListPagosFoliosFacturaView().get(0).getFolioFactura() );
		}
		
		model.setTotalFolioFactura(totalFolio.doubleValue());
		model.setTotalFolioFacturaStr( df.format(totalFolio.doubleValue()) );
		
		model.setTotalOtrosPagos(totalOtrosPagos.doubleValue());
		model.setTotalOtrosPagosStr( df.format(totalOtrosPagos.doubleValue()) );
		
		model.setTotalRemanenteFolioFactura(totalRemanenteFolioFactura.doubleValue());
		String format = df.format(totalRemanenteFolioFactura.doubleValue());
		if (format.contains("(")) {
			format = format.replace("(", "-").replace(")", "");
		}
		model.setTotalRemanenteFolioFacturaStr( format );
		
		
		model.setImportePagoComplemento(importePago.doubleValue());
		model.setImportePagoComplementoStr( df.format(importePago.doubleValue()) );
		return model;
	}
	

	@Override
	public void asignarComplementoPagoFolio(Integer idPagoComplemento, Integer folioFactura, EComplementoPago eComplementoPago) {
		boolean realizaBusqueda = true;
		PagoComplementoDetalleModel model = this.getDetalleComplementoPagoPorFolio(idPagoComplemento, folioFactura, realizaBusqueda, eComplementoPago);
		if (model.getListPagoComplementos() != null) {
			for (PagoComplementoFolioFacturaModel modelFolio : model.getListPagoComplementos()) {
				if ( !modelFolio.isPersistente() ) {
					this.pagoComplementoFolioFactura2Service.registrarPagoComplementoFolioFactura(modelFolio);
					this.actualizarEstatusTimbrado(idPagoComplemento,EComplementoPago.COMPLEMENTO_EN_PROCESO.getEstatus());
				}
			}
		}
		
	}

	@Override
	public void actualizarEstatusTimbrado(Integer idPagoComplemento, String estatus) {
		this.complementoPagoRepository.actualizarEstatusTimbrado(idPagoComplemento, estatus);
	}
	
	@Override
	public void actualizarSaldosPagoComplemento(Integer idPagoComplemento, PagoComplementoDetalleModel model) {
		PagoComplementoEntity pagoComplemento = this.getPagoComplemento(Integer.valueOf(idPagoComplemento));
		//Double totalFacturas = model.getTotalFacturas();
		//Double diferencia = 0.0;
		
		Double importeComplemento = 0.0;
		Double saldoPendiente = 0.0;
		if (pagoComplemento != null) {
			importeComplemento = pagoComplemento.getImporte().doubleValue();
		}
		
		/*diferencia = importeComplemento - totalFacturas ;
		if (diferencia > 0) {
			saldoPendiente = diferencia;
		}*/
		String rfc = seguridadService.encriptar(model.getRfc());
		this.pagoComplementoFolioFactura2Service.actualizarSaldosPagoComplemento(idPagoComplemento, importeComplemento, saldoPendiente, rfc);
	}
	
	@Override
	public void actualizarSaldosPagoComplemento(Integer idPagoComplemento, Double totalFacturas, Double saldoPendiente,
			String rfc) {
		this.pagoComplementoFolioFactura2Service.actualizarSaldosPagoComplemento(idPagoComplemento, totalFacturas, saldoPendiente, rfc);
	}
	
	@Override
	public PagoComplementoEntity getPagoComplemento(Integer idPagoComplemento) {
		PagoComplementoEntity pagoComplementoEntity = null;
		Optional<PagoComplementoEntity> optionalPagoComplemento = this.complementoPagoRepository.findById(idPagoComplemento);
		if(optionalPagoComplemento.isPresent()) {
			pagoComplementoEntity = optionalPagoComplemento.get();
		}
		return pagoComplementoEntity;
	}
	
	@Override
	public void desvincularPagoComplemento(Integer idPagoComplemento) {
		this.pagoComplementoFolioFactura2Service.desvincularPagoComplemento(idPagoComplemento);
	}

	private List<PagoComplementoFolioFacturaModel> merge(List<FolioFacturaModel> listFoliosFactura
			, List<PagoComplementoFolioFacturaModel> listPagoComplementos
			, Integer idFolioFactura
			, Double totalFolio
			, PagoComplementoEntity pagoComplementoEntity ) {
		
		List<PagoComplementoFolioFacturaModel> listPagoComplementosRegistrar = new ArrayList<PagoComplementoFolioFacturaModel>();
		Double importePago = pagoComplementoEntity.getImporte().doubleValue();
		Integer idPagoComplemento = pagoComplementoEntity.getId();
		
		
		if (listPagoComplementos.size() == 0) {
			logger.info("merge() - primer complemento para idPagoComplemento: {}, idFolioFactura: {}, importePago: {}, totalFolio: {}, facturas: {}",
					idPagoComplemento, idFolioFactura, importePago, totalFolio, listFoliosFactura.size());

			PagoComplementoFolioFacturaModel pagoComplemento = new PagoComplementoFolioFacturaModel();
			pagoComplemento.setIdPagoComplemento( idPagoComplemento );
			pagoComplemento.setIdFolioFactura( idFolioFactura );
			pagoComplemento.setFolioFactura( listFoliosFactura.get(0).getFolioFactura() );
			pagoComplemento.setTotalFolioFactura( totalFolio );
			pagoComplemento.setMontoPago( importePago );
			
			List<PagoComplementoFolioFacturaDetalleModel> listPagosComplementoDet = new ArrayList<PagoComplementoFolioFacturaDetalleModel>();
			
			boolean complementaOtraFactura = false;
			Double montoTotalFacturas = 0.0;
			
			for (FolioFacturaModel facturaTotal : listFoliosFactura) {
				int parcialidad = 1;
				Double montoFactura = facturaTotal.getMontoFactura();
				Double montoTotalNC = facturaTotal.getMontoTotalNC();
				Double montoRealFactura = facturaTotal.getMontoRealFactura();
				Double montoSaldoAnterior = 0.0;
				Double importePosterior = 0.0;
				Double importePagado = 0.0;
				
				montoTotalFacturas = montoTotalFacturas + montoFactura;
				montoSaldoAnterior = montoFactura;
				
				Double complementoCubierto = 0.0;
				if (complementaOtraFactura) {
					complementoCubierto = montoTotalFacturas - importePago;
				} else {
					complementoCubierto = montoFactura - importePago;
				}
				
				if (!complementaOtraFactura && complementoCubierto >= 0.0) {
					
					importePagado = importePago;
					importePosterior = montoFactura - importePago;
					
					PagoComplementoFolioFacturaDetalleModel pagoComplementoFolioFacturaDetalle = new PagoComplementoFolioFacturaDetalleModel();
					pagoComplementoFolioFacturaDetalle.setOrden( facturaTotal.getOrden() );
					pagoComplementoFolioFacturaDetalle.setIdPagoComplementoFolioFactura( pagoComplemento.getIdPagoComplementoFolioFactura() );
					pagoComplementoFolioFacturaDetalle.setIdFactura( facturaTotal.getIdFactura() );
					pagoComplementoFolioFacturaDetalle.setParcialidad( parcialidad );
					pagoComplementoFolioFacturaDetalle.setEquivalencia(1.0);
					pagoComplementoFolioFacturaDetalle.setMonedaPago(MONEDA_MX);
					pagoComplementoFolioFacturaDetalle.setMontoPorPagar( montoFactura );
					pagoComplementoFolioFacturaDetalle.setMontoTotalNC( montoTotalNC );
					pagoComplementoFolioFacturaDetalle.setMontoRealFactura(montoRealFactura);
					pagoComplementoFolioFacturaDetalle.setImporteSaldoAnterior( montoSaldoAnterior );
					pagoComplementoFolioFacturaDetalle.setImportePosterior(importePosterior);
					pagoComplementoFolioFacturaDetalle.setImportePagado( importePagado );
					pagoComplementoFolioFacturaDetalle.setUuid( facturaTotal.getUuid() );
					pagoComplementoFolioFacturaDetalle.setRfc( facturaTotal.getRfc() );
					pagoComplementoFolioFacturaDetalle.setRazonSocial( facturaTotal.getRazonSocial() );
					pagoComplementoFolioFacturaDetalle.setColorPagoComplemento(true);
					
					if (complementoCubierto > 0.0) {
						pagoComplementoFolioFacturaDetalle.setEstatusFactura(EFacturaComplemento.PARTES);
					} else {
						pagoComplementoFolioFacturaDetalle.setEstatusFactura(EFacturaComplemento.COMPLETO);
					}
					listPagosComplementoDet.add(pagoComplementoFolioFacturaDetalle);
					break;
				} else if (complementaOtraFactura && complementoCubierto >= 0.0) {
					
					importePagado = importePago - (montoTotalFacturas-montoFactura);
					importePosterior = montoFactura - importePagado;
					
					PagoComplementoFolioFacturaDetalleModel pagoComplementoFolioFacturaDetalle = new PagoComplementoFolioFacturaDetalleModel();
					pagoComplementoFolioFacturaDetalle.setOrden( facturaTotal.getOrden() );
					pagoComplementoFolioFacturaDetalle.setIdPagoComplementoFolioFactura( pagoComplemento.getIdPagoComplementoFolioFactura() );
					pagoComplementoFolioFacturaDetalle.setIdFactura( facturaTotal.getIdFactura() );
					pagoComplementoFolioFacturaDetalle.setParcialidad( parcialidad );
					pagoComplementoFolioFacturaDetalle.setEquivalencia(1.0);
					pagoComplementoFolioFacturaDetalle.setMonedaPago(MONEDA_MX);
					pagoComplementoFolioFacturaDetalle.setMontoPorPagar( montoFactura );
					pagoComplementoFolioFacturaDetalle.setMontoTotalNC( montoTotalNC );
					pagoComplementoFolioFacturaDetalle.setMontoRealFactura(montoRealFactura);
					pagoComplementoFolioFacturaDetalle.setImporteSaldoAnterior( montoSaldoAnterior );
					pagoComplementoFolioFacturaDetalle.setImportePosterior(importePosterior);
					pagoComplementoFolioFacturaDetalle.setImportePagado( importePagado );
					pagoComplementoFolioFacturaDetalle.setUuid( facturaTotal.getUuid() );
					pagoComplementoFolioFacturaDetalle.setRfc( facturaTotal.getRfc() );
					pagoComplementoFolioFacturaDetalle.setRazonSocial( facturaTotal.getRazonSocial() );
					pagoComplementoFolioFacturaDetalle.setColorPagoComplemento(true);
					
					if (complementoCubierto > 0.0) {
						pagoComplementoFolioFacturaDetalle.setEstatusFactura(EFacturaComplemento.PARTES);
					} else {
						pagoComplementoFolioFacturaDetalle.setEstatusFactura(EFacturaComplemento.COMPLETO);
					}
					listPagosComplementoDet.add(pagoComplementoFolioFacturaDetalle);
					break;
				} else {
					
					importePagado = montoFactura;
					importePosterior = 0.0;
					
					PagoComplementoFolioFacturaDetalleModel pagoComplementoFolioFacturaDetalle = new PagoComplementoFolioFacturaDetalleModel();
					pagoComplementoFolioFacturaDetalle.setOrden( facturaTotal.getOrden() );
					pagoComplementoFolioFacturaDetalle.setIdPagoComplementoFolioFactura( pagoComplemento.getIdPagoComplementoFolioFactura() );
					pagoComplementoFolioFacturaDetalle.setIdFactura( facturaTotal.getIdFactura() );
					pagoComplementoFolioFacturaDetalle.setParcialidad( parcialidad );
					pagoComplementoFolioFacturaDetalle.setEquivalencia(1.0);
					pagoComplementoFolioFacturaDetalle.setMonedaPago(MONEDA_MX);
					pagoComplementoFolioFacturaDetalle.setMontoPorPagar( montoFactura );
					pagoComplementoFolioFacturaDetalle.setMontoTotalNC( montoTotalNC );
					pagoComplementoFolioFacturaDetalle.setMontoRealFactura(montoRealFactura);
					pagoComplementoFolioFacturaDetalle.setImporteSaldoAnterior( montoSaldoAnterior );
					pagoComplementoFolioFacturaDetalle.setImportePosterior(importePosterior);
					pagoComplementoFolioFacturaDetalle.setImportePagado( importePagado );
					pagoComplementoFolioFacturaDetalle.setUuid( facturaTotal.getUuid() );
					pagoComplementoFolioFacturaDetalle.setRfc( facturaTotal.getRfc() );
					pagoComplementoFolioFacturaDetalle.setRazonSocial( facturaTotal.getRazonSocial() );
					pagoComplementoFolioFacturaDetalle.setColorPagoComplemento(true);
					pagoComplementoFolioFacturaDetalle.setEstatusFactura(EFacturaComplemento.COMPLETO);
					listPagosComplementoDet.add(pagoComplementoFolioFacturaDetalle);
					
					complementaOtraFactura = true;
					parcialidad ++;
				}
				
			}//for (FolioFacturaModel facturaTotal : listFoliosFactura)
			
			pagoComplemento.setPagosComplementoFolioFacturaDet(listPagosComplementoDet);
			listPagoComplementosRegistrar.add(pagoComplemento);
		}//if (listPagoComplementos.size() == 0) 
		else {
			//Del folio buscado, cuando ya existe una factura asociada a X pago, en base al listado de facturas (conforme se van listado),
			//validar si esa factura, ya se cubrio, o si tiene saldo remanente que pueda ser utilizado (listPagoComplementos),
			//Si la factura se encuentra, debemos agregar el registro en cuestión para que se tenga trazabilidad de los pagos realizados 
			//por esa factura. además de esto, se considera el saldo remanente y si aun no se cubre se van iterando las facturas utilizando su monto total
			//para ir cubriendo el importe
			
			
			PagoComplementoFolioFacturaModel pagoComplemento = new PagoComplementoFolioFacturaModel();
			pagoComplemento.setIdPagoComplemento( idPagoComplemento );
			pagoComplemento.setIdFolioFactura( idFolioFactura );
			pagoComplemento.setFolioFactura( listFoliosFactura.get(0).getFolioFactura() );
			pagoComplemento.setTotalFolioFactura( totalFolio );
			pagoComplemento.setMontoPago( importePago );
			
			List<PagoComplementoFolioFacturaDetalleModel> listPagosComplementoDet = new ArrayList<PagoComplementoFolioFacturaDetalleModel>();

			boolean complementaOtraFactura = false;
			Double montoTotalFacturas = 0.0;
			// FIX parcialidad: se calcula como numero de complementos previos + 1
			// (antes se incrementaba por cada registro de detalle, generando valores incorrectos)
			int parcialidad = listPagoComplementos.size() + 1;
			logger.info("merge() - idPagoComplemento: {}, complementos previos: {}, nueva parcialidad: {}",
					idPagoComplemento, listPagoComplementos.size(), parcialidad);

			sigFactura:
			for (FolioFacturaModel facturaTotal : listFoliosFactura) {

				Double montoFactura = facturaTotal.getMontoFactura();
				Double montoFacturaAux = 0.0;
				Double montoTotalNC = facturaTotal.getMontoTotalNC();
				Double montoRealFactura = facturaTotal.getMontoRealFactura();
				Double montoSaldoAnterior = 0.0;
				Double importePosterior = 0.0;
				Double importePosteriorAux = 0.0;
				Double importePagado = 0.0;

				Double remanenteFactura = 0.0;
				boolean facturaUtilizada = false;
				List<PagoComplementoFolioFacturaDetalleModel> listFacturtasAsignadas = complementoFolioFacturaDetalleService.obtenerFacturasByIdFactura(facturaTotal.getIdFactura());
				if (listFacturtasAsignadas != null && listFacturtasAsignadas.size() > 0) {
					facturaUtilizada = true;
					for (int i=0; i<listFacturtasAsignadas.size(); i++) {
						PagoComplementoFolioFacturaDetalleModel pagoComplementoFolioFacturaDetalle = listFacturtasAsignadas.get(i);

						pagoComplementoFolioFacturaDetalle.setOrden( facturaTotal.getOrden() );
						pagoComplementoFolioFacturaDetalle.setColorPagoComplemento(false);
						// Se mantiene la parcialidad original del registro existente (no se reasigna)
						logger.info("merge() - factura existente idFactura: {}, parcialidad original: {}",
								facturaTotal.getIdFactura(), pagoComplementoFolioFacturaDetalle.getParcialidad());

						listPagosComplementoDet.add(pagoComplementoFolioFacturaDetalle);

						if (i == listFacturtasAsignadas.size() -1) {
							remanenteFactura = pagoComplementoFolioFacturaDetalle.getImportePosterior();
							importePosteriorAux = pagoComplementoFolioFacturaDetalle.getImportePosterior();
						}
					}
				}//if (listFacturtasAsignadas != null && listFacturtasAsignadas.size() > 0)
				
				if(facturaUtilizada && remanenteFactura == 0) {
					continue sigFactura; 
				}
				
				montoFacturaAux = (importePosteriorAux > 0 ? importePosteriorAux : montoFactura);
				
				montoTotalFacturas = montoTotalFacturas + montoFacturaAux;
				montoSaldoAnterior = montoFacturaAux;
				
				Double complementoCubierto = 0.0;
				if (complementaOtraFactura) {
					complementoCubierto = montoTotalFacturas - importePago;
				} else {
					complementoCubierto = montoSaldoAnterior - importePago;
				}
				
				if (!complementaOtraFactura && complementoCubierto >= 0.0) {
					
					importePagado = importePago;
					importePosterior = montoFacturaAux - importePago;
					
					PagoComplementoFolioFacturaDetalleModel pagoComplementoFolioFacturaDetalle = new PagoComplementoFolioFacturaDetalleModel();
					pagoComplementoFolioFacturaDetalle.setOrden( facturaTotal.getOrden() );
					pagoComplementoFolioFacturaDetalle.setIdPagoComplementoFolioFactura( pagoComplemento.getIdPagoComplementoFolioFactura() );
					pagoComplementoFolioFacturaDetalle.setIdFactura( facturaTotal.getIdFactura() );
					pagoComplementoFolioFacturaDetalle.setParcialidad( parcialidad );
					pagoComplementoFolioFacturaDetalle.setEquivalencia(1.0);
					pagoComplementoFolioFacturaDetalle.setMonedaPago(MONEDA_MX);
					pagoComplementoFolioFacturaDetalle.setMontoPorPagar( montoFactura );
					pagoComplementoFolioFacturaDetalle.setMontoTotalNC( montoTotalNC );
					pagoComplementoFolioFacturaDetalle.setMontoRealFactura(montoRealFactura);
					pagoComplementoFolioFacturaDetalle.setImporteSaldoAnterior( montoSaldoAnterior );
					pagoComplementoFolioFacturaDetalle.setImportePosterior(importePosterior);
					pagoComplementoFolioFacturaDetalle.setImportePagado( importePagado );
					pagoComplementoFolioFacturaDetalle.setUuid( facturaTotal.getUuid() );
					pagoComplementoFolioFacturaDetalle.setRfc( facturaTotal.getRfc() );
					pagoComplementoFolioFacturaDetalle.setRazonSocial( facturaTotal.getRazonSocial() );
					pagoComplementoFolioFacturaDetalle.setColorPagoComplemento(true);
					
					if (complementoCubierto > 0.0) {
						pagoComplementoFolioFacturaDetalle.setEstatusFactura(EFacturaComplemento.PARTES);
					} else {
						pagoComplementoFolioFacturaDetalle.setEstatusFactura(EFacturaComplemento.COMPLETO);
					}
					listPagosComplementoDet.add(pagoComplementoFolioFacturaDetalle);
					break;
				} else if (complementaOtraFactura && complementoCubierto >= 0.0) {
					
					importePagado = importePago - (montoTotalFacturas-montoFactura);
					importePosterior = montoFactura - importePagado;
					
					PagoComplementoFolioFacturaDetalleModel pagoComplementoFolioFacturaDetalle = new PagoComplementoFolioFacturaDetalleModel();
					pagoComplementoFolioFacturaDetalle.setOrden( facturaTotal.getOrden() );
					pagoComplementoFolioFacturaDetalle.setIdPagoComplementoFolioFactura( pagoComplemento.getIdPagoComplementoFolioFactura() );
					pagoComplementoFolioFacturaDetalle.setIdFactura( facturaTotal.getIdFactura() );
					pagoComplementoFolioFacturaDetalle.setParcialidad( parcialidad );
					pagoComplementoFolioFacturaDetalle.setEquivalencia(1.0);
					pagoComplementoFolioFacturaDetalle.setMonedaPago(MONEDA_MX);
					pagoComplementoFolioFacturaDetalle.setMontoPorPagar( montoFactura );
					pagoComplementoFolioFacturaDetalle.setMontoTotalNC( montoTotalNC );
					pagoComplementoFolioFacturaDetalle.setMontoRealFactura(montoRealFactura);
					pagoComplementoFolioFacturaDetalle.setImporteSaldoAnterior( montoSaldoAnterior );
					pagoComplementoFolioFacturaDetalle.setImportePosterior(importePosterior);
					pagoComplementoFolioFacturaDetalle.setImportePagado( importePagado );
					pagoComplementoFolioFacturaDetalle.setUuid( facturaTotal.getUuid() );
					pagoComplementoFolioFacturaDetalle.setRfc( facturaTotal.getRfc() );
					pagoComplementoFolioFacturaDetalle.setRazonSocial( facturaTotal.getRazonSocial() );
					pagoComplementoFolioFacturaDetalle.setColorPagoComplemento(true);
					
					if (complementoCubierto > 0.0) {
						pagoComplementoFolioFacturaDetalle.setEstatusFactura(EFacturaComplemento.PARTES);
					} else {
						pagoComplementoFolioFacturaDetalle.setEstatusFactura(EFacturaComplemento.COMPLETO);
					}
					listPagosComplementoDet.add(pagoComplementoFolioFacturaDetalle);
					break;
				} else {
					
					importePagado = montoFacturaAux;
					importePosterior = 0.0;
					
					PagoComplementoFolioFacturaDetalleModel pagoComplementoFolioFacturaDetalle = new PagoComplementoFolioFacturaDetalleModel();
					pagoComplementoFolioFacturaDetalle.setOrden( facturaTotal.getOrden() );
					pagoComplementoFolioFacturaDetalle.setIdPagoComplementoFolioFactura( pagoComplemento.getIdPagoComplementoFolioFactura() );
					pagoComplementoFolioFacturaDetalle.setIdFactura( facturaTotal.getIdFactura() );
					pagoComplementoFolioFacturaDetalle.setParcialidad( parcialidad );
					pagoComplementoFolioFacturaDetalle.setEquivalencia(1.0);
					pagoComplementoFolioFacturaDetalle.setMonedaPago(MONEDA_MX);
					pagoComplementoFolioFacturaDetalle.setMontoPorPagar( montoFactura );
					pagoComplementoFolioFacturaDetalle.setMontoTotalNC( montoTotalNC );
					pagoComplementoFolioFacturaDetalle.setMontoRealFactura(montoRealFactura);
					pagoComplementoFolioFacturaDetalle.setImporteSaldoAnterior( montoSaldoAnterior );
					pagoComplementoFolioFacturaDetalle.setImportePosterior(importePosterior);
					pagoComplementoFolioFacturaDetalle.setImportePagado( importePagado );
					pagoComplementoFolioFacturaDetalle.setUuid( facturaTotal.getUuid() );
					pagoComplementoFolioFacturaDetalle.setRfc( facturaTotal.getRfc() );
					pagoComplementoFolioFacturaDetalle.setRazonSocial( facturaTotal.getRazonSocial() );
					pagoComplementoFolioFacturaDetalle.setColorPagoComplemento(true);
					pagoComplementoFolioFacturaDetalle.setEstatusFactura(EFacturaComplemento.COMPLETO);
					listPagosComplementoDet.add(pagoComplementoFolioFacturaDetalle);

					complementaOtraFactura = true;
					// parcialidad ya no se incrementa aqui, es fija para todo el complemento
				}

			}//for (FolioFacturaModel facturaTotal : listFoliosFactura)

			logger.info("merge() - total registros detalle generados: {}, parcialidad asignada: {}",
					listPagosComplementoDet.size(), parcialidad);
			pagoComplemento.setPagosComplementoFolioFacturaDet(listPagosComplementoDet);
			listPagoComplementosRegistrar.add(pagoComplemento);

		}
		return listPagoComplementosRegistrar;
	}
	
	@SuppressWarnings("unused")
	private PagoComplementoFolioFacturaDetalleModel getComplementoDetalle(FolioFacturaModel facturaTotal
																	    , PagoComplementoFolioFacturaModel pagoComplemento
																	    , int parcialidad
																	    , Double montoFactura
																	    , Double montoTotalNC
																	    , Double montoRealFactura
																	    , Double importeSaldoAnterior
																	    , Double importePosterior
																	    , Double importePagado) {
		
		PagoComplementoFolioFacturaDetalleModel pagoComplementoFolioFacturaDetalle = new PagoComplementoFolioFacturaDetalleModel();
		pagoComplementoFolioFacturaDetalle.setIdPagoComplementoFolioFactura( pagoComplemento.getIdPagoComplementoFolioFactura() );
		pagoComplementoFolioFacturaDetalle.setIdFactura( facturaTotal.getIdFactura() );
		pagoComplementoFolioFacturaDetalle.setParcialidad( parcialidad );
		pagoComplementoFolioFacturaDetalle.setEquivalencia(1.0);
		pagoComplementoFolioFacturaDetalle.setMonedaPago(MONEDA_MX);
		pagoComplementoFolioFacturaDetalle.setMontoPorPagar( montoFactura );
		pagoComplementoFolioFacturaDetalle.setMontoTotalNC(montoTotalNC);
		pagoComplementoFolioFacturaDetalle.setMontoRealFactura(montoRealFactura);
		pagoComplementoFolioFacturaDetalle.setUuid( facturaTotal.getUuid() );
		pagoComplementoFolioFacturaDetalle.setRfc( facturaTotal.getRfc() );
		pagoComplementoFolioFacturaDetalle.setRazonSocial( facturaTotal.getRazonSocial() );
		pagoComplementoFolioFacturaDetalle.setColorPagoComplemento(true);
		
		pagoComplementoFolioFacturaDetalle.setImporteSaldoAnterior( importeSaldoAnterior ); 
		pagoComplementoFolioFacturaDetalle.setImportePosterior( importePosterior );
		pagoComplementoFolioFacturaDetalle.setImportePagado( importePagado );
		return pagoComplementoFolioFacturaDetalle;
	}
	
	private PagoComplementoFolioFacturaViewModel getComplementoFacturaInitModel(PagoComplementoFolioFacturaModel pagoComplemento
			, PagoComplementoFolioFacturaDetalleModel comFolioDetalle
			, int index) {
		PagoComplementoFolioFacturaViewModel complementoFacturaModel = new PagoComplementoFolioFacturaViewModel();
		complementoFacturaModel.setIdPagoComplementoFolioFactura(pagoComplemento.getIdPagoComplementoFolioFactura());
		complementoFacturaModel.setIdPagoComplemento(pagoComplemento.getIdPagoComplemento());
		complementoFacturaModel.setIdFolioFactura( pagoComplemento.getIdFolioFactura() );
		complementoFacturaModel.setIdFactura( comFolioDetalle.getIdFactura() );
		complementoFacturaModel.setFolioFactura( pagoComplemento.getFolioFactura() );
		complementoFacturaModel.setRfc( comFolioDetalle.getRfc() );
		complementoFacturaModel.setRazonSocial( comFolioDetalle.getRazonSocial() );
		complementoFacturaModel.setUuid( comFolioDetalle.getUuid() );
		complementoFacturaModel.setFolio( Integer.valueOf( pagoComplemento.getFolioFactura() ));
		complementoFacturaModel.setMontoPorPagar( comFolioDetalle.getMontoPorPagar());
		complementoFacturaModel.setMontoPorPagarStr( df.format(comFolioDetalle.getMontoPorPagar()));
		
		complementoFacturaModel.setMontoTotalNC(comFolioDetalle.getMontoTotalNC());
		complementoFacturaModel.setMontoTotalNCStr(df.format( comFolioDetalle.getMontoTotalNC() ));
		
		complementoFacturaModel.setMontoRealFactura(comFolioDetalle.getMontoRealFactura());
		complementoFacturaModel.setMontoRealFacturaStr(df.format( comFolioDetalle.getMontoRealFactura() ));
		
		complementoFacturaModel.setPagoComplemento(pagoComplemento.getMontoPago());
		complementoFacturaModel.setPagoComplementoStr( df.format(pagoComplemento.getMontoPago()));
		
		complementoFacturaModel.setRegistro( index + 1 );
		
		complementoFacturaModel.setMonedaPago( comFolioDetalle.getMonedaPago() );
		complementoFacturaModel.setEquivalencia( comFolioDetalle.getEquivalencia() );
		complementoFacturaModel.setParcialidad( comFolioDetalle.getParcialidad() );
		
		complementoFacturaModel.setImporteSaldoAnterior( comFolioDetalle.getImporteSaldoAnterior() );
		complementoFacturaModel.setImporteSaldoAnteriorStr( df.format(comFolioDetalle.getImporteSaldoAnterior()) );
		
		complementoFacturaModel.setImportePagado( comFolioDetalle.getImportePagado() );
		complementoFacturaModel.setImportePagadoStr( df.format(comFolioDetalle.getImportePagado()) );
		
		complementoFacturaModel.setImportePosterior( comFolioDetalle.getImportePosterior() );
		complementoFacturaModel.setImportePosteriorStr( df.format(comFolioDetalle.getImportePosterior()));
		complementoFacturaModel.setOrden( comFolioDetalle.getOrden() );
		
		return complementoFacturaModel;
	}
	
	private PagoComplementoFolioFacturaViewModel getComplementoFacturaDetModel(FolioFacturaModel folioFacturaModel
			, PagoComplementoFolioFacturaDetalleModel comFolioDetalle
			, int index) {
		
		PagoComplementoFolioFacturaModel pagoComplemento = comFolioDetalle.getPagoComplementoFolioFactura();
		PagoComplementoFolioFacturaViewModel complementoFacturaModel = new PagoComplementoFolioFacturaViewModel();
		complementoFacturaModel.setIdPagoComplementoFolioFactura(pagoComplemento.getIdPagoComplementoFolioFactura());
		complementoFacturaModel.setIdPagoComplemento(pagoComplemento.getIdPagoComplemento());
		complementoFacturaModel.setIdFolioFactura( pagoComplemento.getIdFolioFactura() );
		complementoFacturaModel.setIdFactura( comFolioDetalle.getIdFactura() );
		complementoFacturaModel.setFolioFactura( folioFacturaModel.getFolioFactura() );
		complementoFacturaModel.setRfc( folioFacturaModel.getRfc() );
		complementoFacturaModel.setRazonSocial( folioFacturaModel.getRazonSocial() );
		complementoFacturaModel.setUuid( folioFacturaModel.getUuid() );
		complementoFacturaModel.setFolio( Integer.valueOf( folioFacturaModel.getFolioFactura() ));
		complementoFacturaModel.setMontoPorPagar( comFolioDetalle.getMontoPorPagar());
		complementoFacturaModel.setMontoPorPagarStr( df.format(comFolioDetalle.getMontoPorPagar()));
		
		complementoFacturaModel.setMontoTotalNC(comFolioDetalle.getMontoTotalNC());
		complementoFacturaModel.setMontoTotalNCStr(df.format( comFolioDetalle.getMontoTotalNC() ));
		
		complementoFacturaModel.setMontoRealFactura(comFolioDetalle.getMontoRealFactura());
		complementoFacturaModel.setMontoRealFacturaStr(df.format( comFolioDetalle.getMontoRealFactura() ));
		
		complementoFacturaModel.setPagoComplemento(pagoComplemento.getMontoPago());
		complementoFacturaModel.setPagoComplementoStr( df.format(pagoComplemento.getMontoPago()));
		
		complementoFacturaModel.setRegistro( index + 1 );
		
		complementoFacturaModel.setMonedaPago( comFolioDetalle.getMonedaPago() );
		complementoFacturaModel.setEquivalencia( comFolioDetalle.getEquivalencia() );
		complementoFacturaModel.setParcialidad( comFolioDetalle.getParcialidad() );
		
		complementoFacturaModel.setImporteSaldoAnterior( comFolioDetalle.getImporteSaldoAnterior() );
		complementoFacturaModel.setImporteSaldoAnteriorStr( df.format(comFolioDetalle.getImporteSaldoAnterior()) );
		
		complementoFacturaModel.setImportePagado( comFolioDetalle.getImportePagado() );
		complementoFacturaModel.setImportePagadoStr( df.format(comFolioDetalle.getImportePagado()) );
		
		complementoFacturaModel.setImportePosterior( comFolioDetalle.getImportePosterior() );
		complementoFacturaModel.setImportePosteriorStr( df.format(comFolioDetalle.getImportePosterior()));
		complementoFacturaModel.setOrden( comFolioDetalle.getOrden() );
		
		return complementoFacturaModel;
	}
	
	private Double getValue(Double value) {
		if (value == null) {
			return 0.0;
		}
		return value;
	}
}

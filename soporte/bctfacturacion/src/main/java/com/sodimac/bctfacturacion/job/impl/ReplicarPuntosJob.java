package com.sodimac.bctfacturacion.job.impl;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Calendar;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Component;

import com.sodimac.bctfacturacion.enums.EEstatusContable;
import com.sodimac.bctfacturacion.enums.EEstatusControlVenta;
import com.sodimac.bctfacturacion.job.SodimacJob;
import com.sodimac.bctfacturacion.model.AdminPuntosCesModel;
import com.sodimac.bctfacturacion.model.ListaPuntosSkuModel;
import com.sodimac.bctfacturacion.model.PuntosSkuModel;
import com.sodimac.bctfacturacion.model.VentaCabModel;
import com.sodimac.bctfacturacion.model.VentaDetImpuestoModel;
import com.sodimac.bctfacturacion.service.ControlVentaCesService;
import com.sodimac.bctfacturacion.service.IAdminPuntosCesService;
import com.sodimac.bctfacturacion.service.ITrxPointsService;
import com.sodimac.bctfacturacion.service.IVentaCabService;
import com.sodimac.bctfacturacion.service.IVentaDetImpuestoService;

@Component
public class ReplicarPuntosJob implements SodimacJob {

	private static final Logger logger = LoggerFactory.getLogger(ReplicarPuntosJob.class);
	private SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
	
	@Autowired
	private ITrxPointsService trxPointsService;
	
	@Autowired
	private IAdminPuntosCesService adminPuntosCesService;
	
	@Autowired
	private IVentaCabService ventaCabService;
	
	@Autowired
	private IVentaDetImpuestoService ventaDetImpuestoService;
	
	@Autowired
	private ControlVentaCesService controlVentaCeservice;
		
	@Autowired
	private Environment env;
	
	@Override
	public void sincroniza() throws ParseException  {
		
		boolean rangoFechas = Boolean.valueOf(env.getProperty("puntos.ces.rango.fechas")).booleanValue();
		int diasAtraso = Integer.valueOf( env.getProperty("puntos.ces.dias") ).intValue();
		String fechaInicio = env.getProperty("puntos.ces.fecha.inicio");
		String fechaFin = env.getProperty("puntos.ces.fecha.fin");
		
		if (rangoFechas) {
			this.sincronizar(fechaInicio, fechaFin);
		} else {
			this.sincronizar(diasAtraso);
		}
		
	}
	
	private void sincronizar(int diasAtraso) throws ParseException {
		Calendar calInicio = Calendar.getInstance();
		Calendar calFin = Calendar.getInstance();
		
		calFin.add(Calendar.DATE, -1); //DIA VENCIDO
		calInicio.add(Calendar.DATE, diasAtraso * -1);
		
		
		String fechaInicio = sdf.format(calInicio.getTime());
		String fechaFin = sdf.format(calFin.getTime());
		logger.info("Inicia sincronización");
		logger.info("fechaInicio: " + fechaInicio);
		logger.info("fechaFin: " + fechaFin);
		 
		this.sincronizar(fechaInicio,fechaFin);
		 
	    logger.info("Finaliza sincronización");
	}
	
	
	public void sincronizar(String pFechaInicio, String fechaFin) throws ParseException {
	    logger.info("Fecha a sincronizar: " + pFechaInicio + " - " + fechaFin);
	    Calendar calInicio = Calendar.getInstance();
	    Calendar calFin = Calendar.getInstance();
	    calInicio.setTime(this.sdf.parse(pFechaInicio));
	    calFin.setTime(this.sdf.parse(fechaFin));
	    while (calInicio.getTime().compareTo(calFin.getTime()) <= 0) {
	      String fechaSincroniza = this.sdf.format(calInicio.getTime());
	      logger.info("Inicia sincronizacide Venta CES");
	      logger.info("FECHA: " + fechaSincroniza);
	      this.sincronizar(fechaSincroniza);
	      calInicio.add(Calendar.DATE, 1);
	      logger.info("Finaliza sincronizacide Venta CES");
	    } 
	  }
	
	public void sincronizar(String pFecha) throws ParseException {
	    logger.info("Fecha a sincronizar: " + pFecha);
	    Integer idControlCes = null;
	    boolean existeControlCES = this.controlVentaCeservice.existeControlCes(pFecha);
	    if (existeControlCES) {
	      idControlCes = this.controlVentaCeservice.getIdControlCesPorFecha(pFecha);
	      logger.info("idControlCes: " + idControlCes);
	      logger.info("Ya existe control de Venta Ces: " + idControlCes);
	      Integer estatusControlCESPorFecha = this.controlVentaCeservice.getEstatusControlCesPorFecha(idControlCes);
	      EEstatusControlVenta estatus = EEstatusControlVenta.getEstatusById(estatusControlCESPorFecha);
	      logger.info("Estatus [" + idControlCes + "]: " + estatus);
	      if (estatus.getId() == EEstatusControlVenta.CORRECTO.getId()) {
	        logger.info("La fecha se encuentra completa, pasa a la siguiente");
	      } else {
	        logger.info("Fecha incompleta, continua flujo");
	        this.sincronizaIdControl(idControlCes, pFecha);
	      } 
	    } else {
	    	this.sincronizaIdControl(idControlCes, pFecha);
	    } 
	}
	
	private void sincronizaIdControl(Integer idControlCes, String pFecha) throws ParseException {
		
		List<AdminPuntosCesModel> listPuntosCes = trxPointsService.obtenerPuntosCes(pFecha);	
		int i = 0;
		if (listPuntosCes != null && listPuntosCes.size() > 0) {
			logger.info("#### Total de registros a sincronizar: " + listPuntosCes.size() + " ###");
			if (idControlCes == null) {
			    idControlCes = this.controlVentaCeservice.getIdControlCes();
			    logger.info("idControlCes: " + idControlCes);
			    this.controlVentaCeservice.registraControlCes(idControlCes, pFecha, Integer.valueOf(listPuntosCes.size()));
			    logger.info("registra control de Puntos CES: " + idControlCes);
			}
			
			String[] tiposPermitidos = this.controlVentaCeservice.getTipoTransCesPermitidos();
			
			for (AdminPuntosCesModel adminPuntosCes : listPuntosCes) {
				logger.info("Registro: " + i);
		        logger.info(adminPuntosCes.toString());
		        boolean existeAdminVentaCESCab = this.adminPuntosCesService.existeTicket(adminPuntosCes.getTicket()
		        																  , adminPuntosCes.getTipoTransaccionCes());
		        logger.info("existeAdminVentaCESCab: " + existeAdminVentaCESCab);
		        
		        if (!existeAdminVentaCESCab) {
		        	
		        	if (adminPuntosCes.getMontoPuntos() == null) {
		        		logger.info("Ticket con MontoPuntos en nulo: " + adminPuntosCes.getTicket());
		        		continue;
		        	}
		        	
		        	Long idPuntosCes = Long.valueOf(adminPuntosCes.getId() );
					ListaPuntosSkuModel listSkuPuntos = this.trxPointsService.obtenerSkuPuntos(idPuntosCes);
					
					this.adminPuntosCesService.guardar(adminPuntosCes);
					
					double factor = adminPuntosCes.getMontoPuntos()/adminPuntosCes.getPuntos();
										
					if (Arrays.asList(tiposPermitidos).contains(adminPuntosCes.getTipoTransaccionCes())) {
						
		        		List<VentaCabModel> listTicketBct = this.trxPointsService.obtenerTicket( adminPuntosCes.getTicket() );
		        		if (listTicketBct != null) {
		        			for (VentaCabModel ventaCab : listTicketBct) {
		        				
		        				List<VentaDetImpuestoModel> listImpuestosBct = this.trxPointsService.obtenerImpuestos(ventaCab.getTicket());
		        				boolean existeTicket = this.ventaCabService.existeTicket(ventaCab.getTicket());
		        				
			        			if (!existeTicket) {
			        				Integer idVentaCab = this.ventaCabService.obtenerIdVentaCab();
			        				ventaCab.setIdVentaCab(idVentaCab);
			        				ventaCab.setIdPuntosCes(idPuntosCes);
			        				ventaCab.setTipoTransaccionCes( adminPuntosCes.getTipoTransaccionCes() );
			        				ventaCab.setEstatusContable( EEstatusContable.NO_CONTABILIZADO.getId() );
			        				
			        				this.ventaCabService.guardar(ventaCab);
			        				
			        				if (listImpuestosBct != null) {
			        					for (VentaDetImpuestoModel ventaDetImp : listImpuestosBct) {
			        						
			        						boolean existeVentaDetImpuesto = this.ventaDetImpuestoService.existeVentaDetImpuesto(ventaDetImp.getTicket(), ventaDetImp.getNumLinea());
			        						if (!existeVentaDetImpuesto) {
			        							Integer puntos = null;
			        							Integer idVentaDetImpuesto = this.ventaDetImpuestoService.obtnerIdVentaDetImpuesto();
			        							PuntosSkuModel puntosSku = listSkuPuntos.getPuntosSku(ventaDetImp.getSku());
			        							
			        							if (puntosSku != null) {
				        							puntos = puntosSku.getPuntos();
				        						}
			        							
			        							ventaDetImp.setIdVentaCab(idVentaCab);
			        							ventaDetImp.setIdVentaDetImpuesto(idVentaDetImpuesto);
			        							ventaDetImp.setPuntos(puntos);
			        							ventaDetImp.setMontoPuntos(puntos * factor);
			        							
			        							this.ventaDetImpuestoService.guardar(ventaDetImp);
			        						} //if (!existeVentaDetImpuesto)
			        					} //for (VentaDetImpuestoModel ventaDetImp : listImpuestosBct)
			        				}
			        			} //if (!existeTicket)
		        			} //for (VentaCabModel ventaCab : listTicketBct) 
		        			
		        		}
					}
					
	        		
		        } //if (!existeAdminVentaCESCab)
		        i++;
			}
			
			logger.info("marcando registro de control");
		    this.controlVentaCeservice.actualizaControlCes(idControlCes, Integer.valueOf(EEstatusControlVenta.CORRECTO.getId()), Integer.valueOf(i));
		}
	  }
}

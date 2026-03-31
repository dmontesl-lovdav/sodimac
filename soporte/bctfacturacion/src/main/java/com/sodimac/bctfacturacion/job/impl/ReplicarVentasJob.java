package com.sodimac.bctfacturacion.job.impl;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Component;

import com.sodimac.bctfacturacion.job.SodimacJob;
import com.sodimac.bctfacturacion.model.DevolucionCabecera;
import com.sodimac.bctfacturacion.model.DevolucionDetalle;
import com.sodimac.bctfacturacion.model.VentasBct;
import com.sodimac.bctfacturacion.service.DevolucionesBctService;
import com.sodimac.bctfacturacion.service.VentaMSICabService;

@Component
public class ReplicarVentasJob implements SodimacJob {

	private static final Logger logger = LoggerFactory.getLogger(ReplicarVentasJob.class);
	private SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
	
	@Autowired
	private DevolucionesBctService devolucionesBctService;
	
	@Autowired
	private VentaMSICabService ventaMSICabService;
	
	@Autowired
	private Environment env;
	
	@Override
	public void sincroniza() {
		
		boolean rangoFechas = Boolean.valueOf(env.getProperty("venta.cab.rango.fechas")).booleanValue();
		int diasAtraso = Integer.valueOf( env.getProperty("venta.cab.dias") ).intValue();
		String fechaInicio = env.getProperty("venta.cab.fecha.inicio");
		String fechaFin = env.getProperty("venta.cab.fecha.fin");
		
		if (rangoFechas) {
			this.sincronizar(fechaInicio, fechaFin);
		} else {
			this.sincronizar(diasAtraso);
		}
		
	}
	
	private void sincronizar(int diasAtraso) {
		Calendar calInicio = Calendar.getInstance();
		Calendar calFin = Calendar.getInstance();
		
		calFin.add(Calendar.DATE, 0); //DIA VENCIDO
		calInicio.add(Calendar.DATE, diasAtraso * -1);
		
		
		String fechaInicio = sdf.format(calInicio.getTime());
		String fechaFin = sdf.format(calFin.getTime());
		logger.info("Inicia sincronización");
		logger.info("fechaInicio: " + fechaInicio);
		logger.info("fechaFin: " + fechaFin);
		 
		this.sincronizar(fechaInicio,fechaFin);
		 
	    logger.info("Finaliza sincronización");
	}
	
	private void sincronizar(String pFecha, String pFechaFin) {
		logger.info("Fecha a sincronizar: " + pFecha + " " + pFechaFin);
		List<VentasBct> listVenta = this.devolucionesBctService.findVentasPorAnio(pFecha, pFechaFin);
		if (listVenta != null) {
			int i=0;
			for (VentasBct venta : listVenta) {
				
				logger.info("Registro: " + i + " de " + listVenta.size());
				
				String fechaStr = sdf.format( venta.getFechaTrx() );
				Integer tienda = venta.getTienda();
				
				logger.info("Fecha: " + fechaStr );
				logger.info("Tienda: " + tienda );
				
				Integer totalVentaBct = this.devolucionesBctService.getTotalDevolucionesCabecera(fechaStr, tienda);
				Integer totalVentaDetBct = this.devolucionesBctService.getTotalDevolucionDetalle(fechaStr, tienda);
				logger.info("Total de ticket CAB BCT: " + totalVentaBct);
				logger.info("Total de ticket DET en BCT: " + totalVentaDetBct);
				
				Integer totalTickets = this.ventaMSICabService.totalTickets(fechaStr, tienda);
				Integer totalTicketsDet = this.ventaMSICabService.totalTicketsDet(fechaStr, tienda);
				logger.info("Total de ticket CAB fiscal: " + totalTickets);
				logger.info("Total de ticket DET en fiscal: " + totalTicketsDet);
				
				if (totalTickets.intValue() == totalVentaBct.intValue()) {
					logger.info("CAB [" + fechaStr + "] iguales");
				} else {
					List<DevolucionCabecera> listVentas = this.devolucionesBctService.getDevolucionesCabecera(fechaStr, tienda);
					logger.info("Se eliminaran los datos de CAB");
					this.ventaMSICabService.eliminaVentaCab(venta.getFechaTrx(), tienda);
					for (DevolucionCabecera cabecera : listVentas) {
						//logger.info("Se registran las cabeceras");
						this.ventaMSICabService.registraVentaCab(cabecera);
					}
					logger.info("Se registran las cabeceras: " + listVentas.size());
				}
				
				if (totalTicketsDet.intValue() == totalVentaDetBct.intValue()) {
					logger.info("DET [" + fechaStr + "] iguales");
				} else {
					List<DevolucionDetalle> listVentasDetalle = this.devolucionesBctService.getDevolucionDetalle(fechaStr, tienda);
					logger.info("Se eliminaran los datos de DET");
					this.ventaMSICabService.eliminaVentaDet(venta.getFechaTrx(), tienda);
					for (DevolucionDetalle detalle : listVentasDetalle) {
						//logger.info("Se registra el detalle");
						this.ventaMSICabService.registraVentaCabDet(detalle);
					}
					logger.info("Se registran los detalles: " + listVentasDetalle.size());
				}
				
				i ++;
			}
		}
	}
}

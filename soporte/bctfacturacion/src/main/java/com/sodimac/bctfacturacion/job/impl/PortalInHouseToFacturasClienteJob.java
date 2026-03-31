package com.sodimac.bctfacturacion.job.impl;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Component;

import com.sodimac.bctfacturacion.entity.facturacion.FacturasEntity;
import com.sodimac.bctfacturacion.enums.EProcesos;
import com.sodimac.bctfacturacion.job.SodimacJob;
import com.sodimac.bctfacturacion.model.FacturaModel;
import com.sodimac.bctfacturacion.service.FacturacionClienteService;
import com.sodimac.bctfacturacion.service.FacturasService;

@Component
public class PortalInHouseToFacturasClienteJob implements SodimacJob {

	private static final Logger logger = LoggerFactory.getLogger(PortalInHouseToFacturasClienteJob.class);
	private SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
	
	@Autowired
	private FacturasService facturasService;
	
	@Autowired
	private FacturacionClienteService facturacionClienteService;
	
	@Autowired
	private Environment env;
	
	@Override
	public void sincroniza() {
		
		boolean rangoFechas = Boolean.valueOf(env.getProperty("factutas.to.fiscal.rango.fechas")).booleanValue();
		int diasAtraso = Integer.valueOf( env.getProperty("factutas.to.fiscal.dias") ).intValue();
		String fechaInicio = env.getProperty("factutas.to.fiscal.fecha.inicio");
		String fechaFin = env.getProperty("factutas.to.fiscal.fecha.fin");
		
		try {
			if (rangoFechas) {
				this.sincronizar(fechaInicio, fechaFin);
			} else {
				this.sincronizar(diasAtraso);
			}
		} catch (ParseException e) {
			logger.error("Error al formatear fecha");
			e.printStackTrace();
		}
	}

	private void sincronizar(String pFechaInicio, String pFechaFin) throws ParseException {
		logger.info("Fecha a sincronizar: " + pFechaInicio + " - " + pFechaFin);
		Calendar calInicio = Calendar.getInstance();
		Calendar calFin = Calendar.getInstance();
		
		calInicio.setTime( sdf.parse(pFechaInicio) );
		calFin.setTime( sdf.parse(pFechaFin) );
		
		while (calInicio.getTime().compareTo(calFin.getTime()) <= 0) {
			String fechaSincroniza = sdf.format(calInicio.getTime());
			 logger.info("Inicia sincronización " + EProcesos.FACTURAS_TO_FISCAL.getDescripcion());
			 logger.info("FECHA: " + fechaSincroniza);
			 
			 this.sincronizar(fechaSincroniza);
			 
			 calInicio.add(Calendar.DATE, 1);
		     logger.info("Finaliza sincronización de " + EProcesos.FACTURAS_TO_FISCAL.getDescripcion());
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
	
	private void sincronizar(String pFecha) throws ParseException {
		
		String fechaTicket = pFecha;
		Calendar calendar = Calendar.getInstance();
		calendar.setTime( sdf.parse(pFecha) );
		calendar.add(Calendar.DATE, 1);
		
		String nextDay = sdf.format( calendar.getTime() );
		List<FacturasEntity> listTicket = this.facturasService.getTicketFecha(fechaTicket, nextDay);
		
		if (listTicket != null) {
			logger.info("Total de ticket a sincronizar: " + listTicket.size());
			int i=1;
			for (FacturasEntity facEnt : listTicket) {
				logger.info("TicketBct: " + facEnt.getTicketBct() + " - [" + i + " de " + listTicket.size() + "]");
				
				Integer existeTicket = this.facturacionClienteService.existeTicket(facEnt.getUuid());
				
				if (existeTicket.intValue() == 0 ) {
					logger.info("El ticket NO existe, se procede a registrar");
					FacturaModel facturaModel = getFacturaModel(facEnt);
					//logger.info("Model: " + facturaModel.toString());
					
					this.facturacionClienteService.registraFacturacionCliente(facturaModel);
					
				} else {
					logger.info("El ticket ya existe");
				}
				
				i++;
			}
		}
	}
	
	private FacturaModel getFacturaModel(FacturasEntity pFacturaEntity) {
		FacturaModel factura = new FacturaModel();
		factura.setIdFactura( pFacturaEntity.getIdFactura() );
		factura.setIdPac( pFacturaEntity.getIdPac() );
		factura.setIdCliente( pFacturaEntity.getIdCliente() );
		factura.setRfc( pFacturaEntity.getRfc() );
		factura.setEmail( pFacturaEntity.getEmail() );
		factura.setTicket( pFacturaEntity.getTicket() );
		factura.setIdVersionFacturaSodimac( pFacturaEntity.getIdVersionFacturaSodimac()) ;
		factura.setIdFacturaPac( pFacturaEntity.getIdFacturaPac() );
		factura.setUuid( pFacturaEntity.getUuid() );
		factura.setFechaTimbrado( pFacturaEntity.getFechaTimbrado() );
		factura.setVersionFacturacionSat( pFacturaEntity.getVersionFacturacionSat() );
		factura.setXml( pFacturaEntity.getXml() );
		factura.setFechaCompra( pFacturaEntity.getFechaCompra() );
		factura.setIdOrigen( pFacturaEntity.getIdOrigen() );
		factura.setIdEstatusFactura( pFacturaEntity.getIdEstatusFactura() );
		factura.setFechaCreacion( pFacturaEntity.getFechaCreacion() );
		factura.setNombreArchivo( pFacturaEntity.getNombreArchivo() );
		factura.setTicketBct( pFacturaEntity.getTicketBct() );
		factura.setVersionFactura( pFacturaEntity.getVersionFactura() );
		factura.setTransaccion( pFacturaEntity.getTransaccion() );
		factura.setNombreObra( pFacturaEntity.getNombreObra() );
		factura.setResponsableObra( pFacturaEntity.getResponsableObra() );
		factura.setIdComprobante( pFacturaEntity.getIdComprobante() );
		factura.setUuidRelacionado( pFacturaEntity.getUuidRelacionado() );
		factura.setSerie( pFacturaEntity.getSerie() );
		if (pFacturaEntity.getFolio() != null) {
			factura.setFolio( pFacturaEntity.getFolio().toString() );
		}
		 
		return factura;
	}
}

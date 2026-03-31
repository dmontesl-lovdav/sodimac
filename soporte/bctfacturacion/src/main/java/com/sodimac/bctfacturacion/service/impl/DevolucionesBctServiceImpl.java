package com.sodimac.bctfacturacion.service.impl;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

//import org.slf4j.Logger;
//import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.sodimac.bctfacturacion.model.DevolucionCabecera;
import com.sodimac.bctfacturacion.model.DevolucionDetalle;
import com.sodimac.bctfacturacion.model.VentasBct;
import com.sodimac.bctfacturacion.repository.bct.DevolucionesBctRepository;
import com.sodimac.bctfacturacion.service.DevolucionesBctService;

@Service
public class DevolucionesBctServiceImpl implements DevolucionesBctService {
	
	//private static final Logger logger = LoggerFactory.getLogger(DevolucionesBctServiceImpl.class);
	
	private static final int VEN_FECHA_TRX = 0;
	private static final int VEN_NUM_TIENDA = 1;
	private static final int VEN_TOTAL = 2;
	
	private static final int CAB_TICKET = 0;
	private static final int CAB_FECHATICKET = 1;
	private static final int CAB_TIENDA = 2;
	private static final int CAB_CAJA = 3;
	private static final int CAB_TRANSACCION = 4;
	private static final int CAB_TIPO = 5;
	private static final int CAB_TOTAL = 6;
	private static final int CAB_SUBTOTAL = 7;
	private static final int CAB_REDONDEO = 8;
	private static final int CAB_TICKETORIGEN = 9;
	private static final int CAB_FECHAENLACE = 10;
	private static final int CAB_FECHACARGA = 11;

	private static final int DET_TICKET = 0;
	private static final int DET_FECHATICKET = 1;
	private static final int DET_TIENDA = 2;
	private static final int DET_CAJA = 3;
	private static final int DET_NUMDOCCANAL = 4;
	private static final int DET_CANALLINIO = 5;
	private static final int DET_TOTALARTICULO = 6;
	private static final int DET_FECHACARGA = 7;

	@Autowired
	private DevolucionesBctRepository devolucionesRepository;
	
	@Override
	public Integer getTotalDevolucionesCabecera(String fecha, Integer tienda) {
		return devolucionesRepository.totalVentas(fecha, tienda);
	}


	@Override
	public Integer getTotalDevolucionDetalle(String pFecha, Integer pTienda) {
		return this.devolucionesRepository.totalVentasDet(pFecha, pTienda);
	}
	
	@Override
	public List<VentasBct> findVentasPorAnio(String pFecha, String pFechaFin){
		List<VentasBct> listVentas = new ArrayList<VentasBct>();
		List<Object[]> listArrCabecera = devolucionesRepository.findVentasPorAnio(pFecha, pFechaFin);
		if (listArrCabecera != null) {
			for (Object[] arrObj : listArrCabecera) {
				
				Date fechaTrx = (arrObj[VEN_FECHA_TRX] != null) ? (Date) arrObj[VEN_FECHA_TRX] : null;
				Integer tienda = (arrObj[VEN_NUM_TIENDA] != null) ? Integer.valueOf(arrObj[VEN_NUM_TIENDA].toString()) : null;
				Integer total = (arrObj[VEN_TOTAL] != null) ? Integer.valueOf(arrObj[VEN_TOTAL].toString()) : null;
				
				VentasBct venta = new VentasBct();
				venta.setFechaTrx(fechaTrx);
				venta.setTienda(tienda);
				venta.setTotal(total);
				
				listVentas.add(venta);
			}
		}
		return listVentas;
	}
	
	
	@Override
	@Transactional("transactionManagerBct")
	public List<DevolucionCabecera> getDevolucionesCabecera(String fecha, Integer pTienda) {
		List<DevolucionCabecera> listCabecera = new ArrayList<DevolucionCabecera>();
		List<Object[]> listArrCabecera = devolucionesRepository.findVentas(fecha, pTienda);
		
		if (listArrCabecera != null) {
			
			//int i = 0;
			for (Object[] arrObj : listArrCabecera) {
				
				String ticket = (arrObj[CAB_TICKET] != null) ? arrObj[CAB_TICKET].toString() : null;
				Date fechaTicket = (arrObj[CAB_FECHATICKET] != null) ? (Date) arrObj[CAB_FECHATICKET] : null;
				Integer tienda = (arrObj[CAB_TIENDA] != null) ? Integer.valueOf(arrObj[CAB_TIENDA].toString()) : null;
				Integer caja = (arrObj[CAB_CAJA] != null) ? Integer.valueOf(arrObj[CAB_CAJA].toString()) : null;
				Integer transaccion = (arrObj[CAB_TRANSACCION] != null) ? Integer.valueOf(arrObj[CAB_TRANSACCION].toString()) : null;
				Integer tipo = (arrObj[CAB_TIPO] != null) ? Integer.valueOf(arrObj[CAB_TIPO].toString()) : null;
				Double total = (arrObj[CAB_TOTAL] != null) ? Double.valueOf(arrObj[CAB_TOTAL].toString()) : null;
				Double subtotal = (arrObj[CAB_SUBTOTAL] != null) ? Double.valueOf(arrObj[CAB_SUBTOTAL].toString()) : null;
				Double redondeo = (arrObj[CAB_REDONDEO] != null) ? Double.valueOf(arrObj[CAB_REDONDEO].toString()) : null;
				String ticketOrigen = (arrObj[CAB_TICKETORIGEN] != null) ? arrObj[CAB_TICKETORIGEN].toString() : null;
				Date fechaEnlace = (arrObj[CAB_FECHAENLACE] != null) ? (Date) arrObj[CAB_FECHAENLACE] : null;
				Date fechaCarga = (arrObj[CAB_FECHACARGA] != null) ? (Date) arrObj[CAB_FECHACARGA] : null;
				Integer estatusProceso = 0;

				DevolucionCabecera credivoucherCab = new DevolucionCabecera();
				credivoucherCab.setTicket(ticket);
				credivoucherCab.setFechaTicket(fechaTicket);
				credivoucherCab.setTienda(tienda);
				credivoucherCab.setCaja(caja);
				credivoucherCab.setTransaccion(transaccion);
				credivoucherCab.setTipo(tipo);
				credivoucherCab.setTotal(total);
				credivoucherCab.setSubtotal(subtotal);
				credivoucherCab.setRedondeo(redondeo);
				credivoucherCab.setTicketOrigen(ticketOrigen);
				credivoucherCab.setFechaEnlace(fechaEnlace);
				credivoucherCab.setFechaCarga(fechaCarga);
				credivoucherCab.setEstatusProceso(estatusProceso);
				
				listCabecera.add( credivoucherCab );
				//logger.info("Registro[" + ticket + "]: " + i + " de " + listArrCabecera.size());
				//i++;
			}
		}
		return listCabecera;
	}
	
	@Override
	@Transactional("transactionManagerBct")
	public List<DevolucionDetalle> getDevolucionDetalle(String pFecha, Integer pTienda) {
		List<DevolucionDetalle> listDetalle = new ArrayList<DevolucionDetalle>();
		List<Object[]> listArrDetalle = devolucionesRepository.findVentasDet(pFecha, pTienda);
		
		if (listArrDetalle != null) {
			for (Object[] arrObj : listArrDetalle) {
				
				String ticket = (arrObj[DET_TICKET] != null) ? arrObj[DET_TICKET].toString() : null;
				Date fechaTicket = (arrObj[DET_FECHATICKET] != null) ? (Date) arrObj[DET_FECHATICKET] : null;
				Integer tienda = (arrObj[DET_TIENDA] != null) ? Integer.valueOf(arrObj[DET_TIENDA].toString()) : null;
				Integer caja = (arrObj[DET_CAJA] != null) ? Integer.valueOf(arrObj[DET_CAJA].toString()) : null;
				String numDocCanal = (arrObj[DET_NUMDOCCANAL] != null) ? arrObj[DET_NUMDOCCANAL].toString() : null;
				String canalLinio = (arrObj[DET_CANALLINIO] != null) ? arrObj[DET_CANALLINIO].toString() : null;
				Integer totalArticulo = (arrObj[DET_TOTALARTICULO] != null) ? Integer.valueOf(arrObj[DET_TOTALARTICULO].toString()) : null;
				Date fechaCarga = (arrObj[DET_FECHACARGA] != null) ? (Date) arrObj[DET_FECHACARGA] : null;
				Integer cajaEstatusProceso = 0;

				
				DevolucionDetalle devolucionDet = new DevolucionDetalle();
				devolucionDet.setTicket(ticket);
				devolucionDet.setFechaTicket(fechaTicket);
				devolucionDet.setTienda(tienda);
				devolucionDet.setCaja(caja);
				devolucionDet.setNumDocCanal(numDocCanal);
				devolucionDet.setCanalLinio(canalLinio);
				devolucionDet.setTotalArticulo(totalArticulo);
				devolucionDet.setFechaCarga(fechaCarga);
				devolucionDet.setCajaEstatusProceso(cajaEstatusProceso);
				
				listDetalle.add( devolucionDet );
			}
		}
		return listDetalle;
	}
}

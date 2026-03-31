package com.sodimac.bctfacturacion.mapper;

import java.text.ParseException;
import java.util.ArrayList;
import java.util.List;

import com.sodimac.bctfacturacion.entity.ces.AdminPuntosCesEntity;
import com.sodimac.bctfacturacion.enums.EEstatus;
import com.sodimac.bctfacturacion.enums.EEstatusContable;
import com.sodimac.bctfacturacion.model.AdminPuntosCesModel;
import com.sodimac.bctfacturacion.util.FechaUtils;

public final class AdminPuntosCesMapper {
	
	private static final int IDX_NUM_TRX 			= 0;
	private static final int IDX_FECHA_TRX 			= 1;
	private static final int IDX_NUM_TIENDA 		= 2;
	private static final int IDX_TIPO_TRX 			= 3;
	private static final int IDX_MNT_TOTAL_A_PAGAR 	= 4;
	private static final int IDX_MNT_TOT_SN_IMPTOS 	= 5;
	private static final int IDX_MNT_REDONDEO 		= 6;
	private static final int IDX_SUM_MNT_TOTAL 		= 7;
	private static final int IDX_ID 				= 8;
	private static final int IDX_TRANSACTIONTYPE 	= 9;
	private static final int IDX_POINTS 			= 10;

	private AdminPuntosCesMapper() {
		super();
	}
	
	public static AdminPuntosCesModel convertToDto(AdminPuntosCesEntity entity) {
		AdminPuntosCesModel model = new AdminPuntosCesModel();
		model.setIdAdminPuntoCes( entity.getIdAdminPuntoCes() );
		model.setTicket( entity.getTicket() );
		model.setPuntos( entity.getPuntos() );
		model.setMontoPuntos( entity.getMontoPuntos() );
		model.setMontoVenta( entity.getMontoVenta() );
		model.setTienda( entity.getTienda() );
		model.setTipoTransaccion( entity.getTipoTransaccion() );
		model.setTipoTransaccionCes( entity.getTipoTransaccionCes() );
		model.setEstatus( entity.getEstatus() );
		model.setEstatusContable( entity.getEstatusContable() );
		model.setFechaRegistro( entity.getFechaRegistro() );
		model.setFechaActualizacion( entity.getFechaActualizacion() );
		
		if (entity.getFechaVenta() != null) {
			model.setFechaVenta( FechaUtils.getStrShort( entity.getFechaVenta() ));
		}
		
		return model;
	}
	
	public static AdminPuntosCesEntity convertToEntity(AdminPuntosCesModel dto) throws ParseException {
		AdminPuntosCesEntity entity = new AdminPuntosCesEntity();
		entity.setIdAdminPuntoCes( dto.getIdAdminPuntoCes() );
		entity.setTicket( dto.getTicket() );
		entity.setPuntos( dto.getPuntos() );
		entity.setMontoPuntos( dto.getMontoPuntos() );
		entity.setMontoVenta( dto.getMontoVenta() );
		entity.setTienda( dto.getTienda() );
		entity.setTipoTransaccion( dto.getTipoTransaccion() );
		entity.setTipoTransaccionCes( dto.getTipoTransaccionCes() );
		entity.setEstatus( dto.getEstatus() );
		entity.setEstatusContable( dto.getEstatusContable() );
		entity.setFechaRegistro( dto.getFechaRegistro() );
		entity.setFechaActualizacion( dto.getFechaActualizacion() );
		
		if (dto.getFechaVenta() != null) {
			entity.setFechaVenta( FechaUtils.getDateShort( dto.getFechaVenta() ));
		}
		return entity;
	}
	
	public static AdminPuntosCesModel convertObjectToDto(Object[] arrObj) {
		AdminPuntosCesModel model = new AdminPuntosCesModel();
		
		String numTrx = (arrObj[IDX_NUM_TRX] != null) ? arrObj[IDX_NUM_TRX].toString() : null;
		String fechaTrx = (arrObj[IDX_FECHA_TRX] != null) ? arrObj[IDX_FECHA_TRX].toString() : null;
		Integer numTienda = (arrObj[IDX_NUM_TIENDA] != null) ? Integer.valueOf(arrObj[IDX_NUM_TIENDA].toString()) : null;
		Integer tipoTrx = (arrObj[IDX_TIPO_TRX] != null) ? Integer.valueOf(arrObj[IDX_TIPO_TRX].toString()) : null;
		Double mntTotalPagar = (arrObj[IDX_MNT_TOTAL_A_PAGAR] != null) ? Double.valueOf(arrObj[IDX_MNT_TOTAL_A_PAGAR].toString()) : null;
		Double mntTotSnImptos = (arrObj[IDX_MNT_TOT_SN_IMPTOS] != null) ? Double.valueOf(arrObj[IDX_MNT_TOT_SN_IMPTOS].toString()) : null;
		Double mntRedondeo = (arrObj[IDX_MNT_REDONDEO] != null) ? Double.valueOf(arrObj[IDX_MNT_REDONDEO].toString()) : null;
		Double sumMntTotal = (arrObj[IDX_SUM_MNT_TOTAL] != null) ? Double.valueOf(arrObj[IDX_SUM_MNT_TOTAL].toString()) : null;
		String id = (arrObj[IDX_ID] != null) ? arrObj[IDX_ID].toString() : null;
		String transactionType = (arrObj[IDX_TRANSACTIONTYPE] != null) ? arrObj[IDX_TRANSACTIONTYPE].toString() : null;
		Integer points = (arrObj[IDX_POINTS] != null) ? Integer.valueOf(arrObj[IDX_POINTS].toString()) : null;
		
		model.setTicket(numTrx);
		model.setFechaVenta(fechaTrx);
		model.setPuntos(points);
		model.setMontoPuntos(sumMntTotal);
		model.setMontoVenta(mntTotalPagar);
		model.setTienda(numTienda);
		model.setFechaRegistro( FechaUtils.getDate() );
		model.setTipoTransaccion( tipoTrx );
		model.setTipoTransaccionCes( transactionType );
		model.setEstatus( EEstatus.ACTIVO.getId() );
		model.setEstatusContable( EEstatusContable.NO_CONTABILIZADO.getId() );
		model.setFechaActualizacion(null);
		
		model.setMontoTotalSinImpuestos(mntTotSnImptos);
		model.setMontoRedondeo(mntRedondeo);
		model.setId(id);
		
		return model;
	}

	public static List<AdminPuntosCesModel> convertToDtos(List<AdminPuntosCesEntity> entities) {
		List<AdminPuntosCesModel> dtos = new ArrayList<>();
		if (entities != null) {
			for (AdminPuntosCesEntity entity : entities) {
				dtos.add( convertToDto(entity) );
			}
		}
		return dtos;
	}

	public static List<AdminPuntosCesModel> convertObjectToDtos(List<Object[]> listObject) {
		List<AdminPuntosCesModel> dtos = new ArrayList<>();
		if (listObject != null) {
			for (Object[] object : listObject) {
				dtos.add( convertObjectToDto(object) );
			}
		}
		return dtos;
	}	
}

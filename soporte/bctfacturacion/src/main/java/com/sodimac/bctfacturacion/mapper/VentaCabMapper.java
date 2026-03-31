package com.sodimac.bctfacturacion.mapper;

import java.text.ParseException;
import java.util.ArrayList;
import java.util.List;

import com.sodimac.bctfacturacion.entity.ces.VentaCabEntity;
import com.sodimac.bctfacturacion.model.VentaCabModel;
import com.sodimac.bctfacturacion.util.FechaUtils;

public final class VentaCabMapper {

	private static final int IDX_NUM_TRX 			= 0;
	private static final int IDX_FECHA_TRX 			= 1;
	private static final int IDX_NUM_TIENDA 		= 2;
	private static final int IDX_TIPO_TRX 			= 3;
	private static final int IDX_MNT_TOTAL_A_PAGAR 	= 4;
	private static final int IDX_MNT_TOT_SN_IMPTOS 	= 5;
	private static final int IDX_MNT_REDONDEO 		= 6;
	
	private VentaCabMapper() {
		super();
	}
	
	public static VentaCabModel convertToDto(VentaCabEntity entity) {
		VentaCabModel  dto = new VentaCabModel();
		dto.setIdVentaCab( entity.getIdVentaCab() );
		dto.setIdPuntosCes( entity.getIdPuntosCes() );
		dto.setTicket( entity.getTicket() );
		dto.setFechaVenta( entity.getFechaVenta() );
		dto.setTienda( entity.getTienda() );
		dto.setTipoTransaccion( entity.getTipoTransaccion() );
		dto.setTipoTransaccionCes( entity.getTipoTransaccionCes() );
		dto.setMontoTotalPagar( entity.getMontoTotalPagar() );
		dto.setMontoTotalSinImpuestos( entity.getMontoTotalSinImpuestos() );
		dto.setMontoRedondeo( entity.getMontoRedondeo() );
		dto.setFechaRegistro( entity.getFechaVenta() );
		dto.setEstatusContable( entity.getEstatusContable() );
		dto.setFechaActualizacion( entity.getFechaActualizacion() );
		return dto;
	}
	
	public static VentaCabEntity convertToEntity(VentaCabModel dto) throws ParseException {
		VentaCabEntity entity = new VentaCabEntity();
		entity.setIdVentaCab( dto.getIdVentaCab() );
		entity.setIdPuntosCes( dto.getIdPuntosCes() );
		entity.setTicket( dto.getTicket() );
		entity.setFechaVenta( dto.getFechaVenta() );
		entity.setTienda( dto.getTienda() );
		entity.setTipoTransaccion( dto.getTipoTransaccion() );
		entity.setTipoTransaccionCes( dto.getTipoTransaccionCes() );
		entity.setMontoTotalPagar( dto.getMontoTotalPagar() );
		entity.setMontoTotalSinImpuestos( dto.getMontoTotalSinImpuestos() );
		entity.setMontoRedondeo( dto.getMontoRedondeo() );
		entity.setFechaRegistro( dto.getFechaVenta() );
		entity.setEstatusContable( dto.getEstatusContable() );
		entity.setFechaActualizacion( dto.getFechaActualizacion() );
		return entity;
	}
	
	public static VentaCabModel convertObjectToDto(Object[] arrObj) throws ParseException {
		String numTrx = (arrObj[IDX_NUM_TRX] != null) ? arrObj[IDX_NUM_TRX].toString() : null;
		String fechaTrx = (arrObj[IDX_FECHA_TRX] != null) ? arrObj[IDX_FECHA_TRX].toString() : null;
		Integer numTienda = (arrObj[IDX_NUM_TIENDA] != null) ? Integer.valueOf(arrObj[IDX_NUM_TIENDA].toString()) : null;
		Integer tipoTrx = (arrObj[IDX_TIPO_TRX] != null) ? Integer.valueOf(arrObj[IDX_TIPO_TRX].toString()) : null;
		Double mntTotalPagar = (arrObj[IDX_MNT_TOTAL_A_PAGAR] != null) ? Double.valueOf(arrObj[IDX_MNT_TOTAL_A_PAGAR].toString()) : null;
		Double mntTotSnImptos = (arrObj[IDX_MNT_TOT_SN_IMPTOS] != null) ? Double.valueOf(arrObj[IDX_MNT_TOT_SN_IMPTOS].toString()) : null;
		Double mntRedondeo = (arrObj[IDX_MNT_REDONDEO] != null) ? Double.valueOf(arrObj[IDX_MNT_REDONDEO].toString()) : null;
		
		VentaCabModel model = new VentaCabModel();
		model.setTicket(numTrx);
		model.setFechaVenta( FechaUtils.getDateShort(fechaTrx) );
		model.setTienda(numTienda);
		model.setTipoTransaccion(tipoTrx);
		model.setMontoTotalPagar(mntTotalPagar);
		model.setMontoTotalSinImpuestos(mntTotSnImptos);
		model.setMontoRedondeo(mntRedondeo);
		
		model.setFechaRegistro( FechaUtils.getDate() );
		
		return model;
	}

	public static List<VentaCabModel> convertObjectToDtos(List<Object[]> listObject) throws ParseException {
		List<VentaCabModel> dtos = new ArrayList<>();
		if (listObject != null) {
			for (Object[] object : listObject) {
				dtos.add( convertObjectToDto(object) );
			}
		}
		return dtos;
	}
	
}

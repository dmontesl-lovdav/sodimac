package com.sodimac.bctfacturacion.mapper;

import java.text.ParseException;
import java.util.ArrayList;
import java.util.List;

import com.sodimac.bctfacturacion.entity.ces.VentaDetImpuestoEntity;
import com.sodimac.bctfacturacion.model.VentaDetImpuestoModel;
import com.sodimac.bctfacturacion.util.FechaUtils;

public final class VentaDetImpuestoMapper {

	private static final int IDX_NUM_TRX 			= 0;
	private static final int IDX_NUM_LINEA 			= 1;
	private static final int IDX_ORDEN_IMPTO 		= 2;
	private static final int IDX_SKU 				= 3;
	private static final int IDX_DV_SKU 			= 4;
	private static final int IDX_DESCRIPCION 		= 5;
	private static final int IDX_TOTAL_LINEA 		= 6;
	private static final int IDX_TIPO_IMPTO 		= 7;
	private static final int IDX_ID_TASA_IMPTO 		= 8;
	private static final int IDX_MNT_IMPTO 			= 9;
	private static final int IDX_PCTJE_IMPTO	 	= 10;
	
	private VentaDetImpuestoMapper() {
		super();
	}
	
	public static VentaDetImpuestoModel converToDto(VentaDetImpuestoEntity entity) {
		VentaDetImpuestoModel dto = new VentaDetImpuestoModel();
		dto.setIdVentaDetImpuesto( entity.getIdVentaDetImpuesto() );
		dto.setIdVentaCab( entity.getIdVentaCab() );
		dto.setTicket( entity.getTicket() );
		dto.setNumLinea( entity.getNumLinea() );
		dto.setOrdenImpuesto( entity.getOrdenImpuesto() );
		dto.setSku( entity.getSku() );
		dto.setDvSku( entity.getDvSku() );
		dto.setDescripcion( entity.getDescripcion() );
		dto.setPuntos( entity.getPuntos() );
		dto.setMontoPuntos(entity.getMontoPuntos());
		dto.setTotalLinea( entity.getTotalLinea() );
		dto.setTipoImpuesto( entity.getTipoImpuesto() );
		dto.setIdTasaImpuesto( entity.getIdTasaImpuesto() );
		dto.setMontoImpuesto( entity.getMontoImpuesto() );
		dto.setPorcentajeImpuesto( entity.getPorcentajeImpuesto() );
		dto.setFechaRegistro( entity.getFechaRegistro() );
		return dto;
	}
	
	public static VentaDetImpuestoEntity converToEntity(VentaDetImpuestoModel dto) throws ParseException {
		VentaDetImpuestoEntity entity = new VentaDetImpuestoEntity();
		entity.setIdVentaDetImpuesto( dto.getIdVentaDetImpuesto() );
		entity.setIdVentaCab( dto.getIdVentaCab() );
		entity.setTicket( dto.getTicket() );
		entity.setNumLinea( dto.getNumLinea() );
		entity.setOrdenImpuesto( dto.getOrdenImpuesto() );
		entity.setSku( dto.getSku() );
		entity.setDvSku( dto.getDvSku() );
		entity.setDescripcion( dto.getDescripcion() );
		entity.setPuntos( dto.getPuntos() );
		entity.setMontoPuntos(dto.getMontoPuntos());
		entity.setTotalLinea( dto.getTotalLinea() );
		entity.setTipoImpuesto( dto.getTipoImpuesto() );
		entity.setIdTasaImpuesto( dto.getIdTasaImpuesto() );
		entity.setMontoImpuesto( dto.getMontoImpuesto() );
		entity.setPorcentajeImpuesto( dto.getPorcentajeImpuesto() );
		entity.setFechaRegistro( dto.getFechaRegistro() );
		return entity;
	}
	
	public static VentaDetImpuestoModel convertObjectToDto(Object[] arrObj) {
		String ticket = (arrObj[IDX_NUM_TRX] != null) ? arrObj[IDX_NUM_TRX].toString() : null;
		Integer numLinea = (arrObj[IDX_NUM_LINEA] != null) ? Integer.valueOf(arrObj[IDX_NUM_LINEA].toString()) : null;
		Integer ordenImpuesto = (arrObj[IDX_ORDEN_IMPTO] != null) ? Integer.valueOf(arrObj[IDX_ORDEN_IMPTO].toString()) : null;
		String sku = (arrObj[IDX_SKU] != null) ? arrObj[IDX_SKU].toString() : null;
		String dvSku = (arrObj[IDX_DV_SKU] != null) ? arrObj[IDX_DV_SKU].toString() : null;
		String descripcion = (arrObj[IDX_DESCRIPCION] != null) ? arrObj[IDX_DESCRIPCION].toString() : null;
		Double totalLinea = (arrObj[IDX_TOTAL_LINEA] != null) ? Double.valueOf(arrObj[IDX_TOTAL_LINEA].toString()) : null;
		Integer tipoImpuesto = (arrObj[IDX_TIPO_IMPTO] != null) ? Integer.valueOf(arrObj[IDX_TIPO_IMPTO].toString()) : null;
		Integer idTasaImpuesto = (arrObj[IDX_ID_TASA_IMPTO] != null) ? Integer.valueOf(arrObj[IDX_ID_TASA_IMPTO].toString()) : null;
		Double montoImpuesto = (arrObj[IDX_MNT_IMPTO] != null) ? Double.valueOf(arrObj[IDX_MNT_IMPTO].toString()) : null;
		Double porcentajeImpuesto = (arrObj[IDX_PCTJE_IMPTO] != null) ? Double.valueOf(arrObj[IDX_PCTJE_IMPTO].toString()) : null;
		
		VentaDetImpuestoModel dto = new VentaDetImpuestoModel();
		dto.setTicket( ticket );
		dto.setNumLinea( numLinea );
		dto.setOrdenImpuesto( ordenImpuesto );
		dto.setSku( sku );
		dto.setDvSku( dvSku );
		dto.setDescripcion( descripcion );
		dto.setTotalLinea( totalLinea );
		dto.setTipoImpuesto( tipoImpuesto );
		dto.setIdTasaImpuesto( idTasaImpuesto );
		dto.setMontoImpuesto( montoImpuesto );
		dto.setPorcentajeImpuesto( porcentajeImpuesto );
		dto.setFechaRegistro( FechaUtils.getDate() );
		return dto;
	}
	
	public static List<VentaDetImpuestoModel> converToDtos(List<VentaDetImpuestoEntity> entities) {
		List<VentaDetImpuestoModel> dtos = new ArrayList<>();
		if (entities != null) {
			for (VentaDetImpuestoEntity entity : entities) {
				dtos.add( converToDto(entity) );
			}
		}
		return dtos;
	}

	public static List<VentaDetImpuestoModel> convertObjectToDtos(List<Object[]> listObject) {
		List<VentaDetImpuestoModel> dtos = new ArrayList<>();
		if (listObject != null) {
			for (Object[] object : listObject) {
				dtos.add( convertObjectToDto(object) );
			}
		}
		return dtos;
	}	
}

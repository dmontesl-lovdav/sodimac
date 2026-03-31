package com.sodimac.	bctfacturacion.mapper;

import java.util.List;

import com.sodimac.bctfacturacion.model.ListaPuntosSkuModel;
import com.sodimac.bctfacturacion.model.PuntosSkuModel;

public final class PuntosSkuMapper {

	private static final int IDX_ID 		= 0;
	private static final int IDX_SKU 		= 1;
	private static final int IDX_POINT 		= 2;
	
	private PuntosSkuMapper() {
		super();
	}
	
	public static PuntosSkuModel convertToDto(Object[] arrObj) {
		Long idPuntosCes = (arrObj[IDX_ID] != null) ? Long.valueOf(arrObj[IDX_ID].toString()) : null;
		String sku = (arrObj[IDX_SKU] != null) ? arrObj[IDX_SKU].toString() : null;
		Integer puntos = (arrObj[IDX_POINT] != null) ? Integer.valueOf(arrObj[IDX_POINT].toString()) : null;
		
		PuntosSkuModel model = new PuntosSkuModel();
		model.setIdPuntosCes(idPuntosCes);
		model.setSku(sku);
		model.setPuntos(puntos);
		return model;
	}
	
	public static ListaPuntosSkuModel convertToDto(List<Object[]> listObjects) {
		ListaPuntosSkuModel dtos = new ListaPuntosSkuModel();
		if (listObjects != null) {
			for (Object[] arrObj : listObjects) {
				dtos.add( convertToDto(arrObj) );
			}
		}
		return dtos;
	}
}

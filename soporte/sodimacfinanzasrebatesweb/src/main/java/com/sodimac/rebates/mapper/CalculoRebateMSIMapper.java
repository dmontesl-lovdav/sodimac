package com.sodimac.rebates.mapper;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;

import com.sodimac.rebates.dto.CalculoRebateMSIDto;

public final class CalculoRebateMSIMapper {

	private static SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy");
	private static SimpleDateFormat sdf2 = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSS");
	private static final int INDEX_ID_CALCULO_REBATE 	= 0;
	private static final int INDEX_ORIGEN 				= 1;
	private static final int INDEX_MONEDA_VENTA 		= 2;
	private static final int INDEX_RFC 					= 3;
	private static final int INDEX_NUMERO_PROVEEDOR 	= 4;
	private static final int INDEX_FAMILIA 				= 5;
	private static final int INDEX_NOMBRE_FAMILIA 		= 6;
	private static final int INDEX_TICKET_VENTA 		= 7;
	private static final int INDEX_SUCURSAL_VENTA 		= 8;
	private static final int INDEX_FECHA_VENTA 			= 9;
	private static final int INDEX_BANCO 				= 10;
	private static final int INDEX_NUM_CUOTA 			= 11;
	private static final int INDEX_SKU 					= 12;
	private static final int INDEX_DESCRIPCION_PRODUCTO = 13;
	private static final int INDEX_SUBTOTAL_SKU 		= 14;
	private static final int INDEX_MONTO_VENTA_SKU 		= 15;
	private static final int INDEX_TIPO_ACUERDO 		= 16;
	private static final int INDEX_MONEDA_ACUERDO 		= 17;
	private static final int INDEX_VALOR_DESCUENTO 		= 18;
	private static final int INDEX_TIPO_DESCUENTO 		= 19;
	private static final int INDEX_MONTO_REBATE 		= 20;
	private static final int INDEX_IVA_REBATE 			= 21;
	private static final int INDEX_MONTO_TOTAL_REBATE 	= 22;
	private static final int INDEX_PROGRAMA_PAGO 		= 23;
	private static final int INDEX_ID_PERIODO 			= 24;
	private static final int INDEX_SUBTOTAL_CUENTA 		= 25;
	private static final int INDEX_IVA_CUENTA 			= 26;
	private static final int INDEX_PROVEEDOR_MERCANCIA 	= 27;
	private static final int INDEX_TIPO_DOCUMENTO_POLIZA = 28;
	private static final int INDEX_CENTRO_COSTOS 		= 29;
	private static final int INDEX_CENTRO_BENEFICIOS 	= 30;
	private static final int INDEX_SUCURSAL 			= 31;
	private static final int INDEX_CONDICIONES_PAGO 	= 32;
	private static final int INDEX_EXCLUSION 			= 33;
	private static final int INDEX_FECHA_EXCLUSION	 	= 34;
	private static final int INDEX_ID_EXCLUSION		 	= 35;
	
	private CalculoRebateMSIMapper() {
		super();
	}
	
	public static CalculoRebateMSIDto convertToDto(Object[] arrObj) throws ParseException {
		CalculoRebateMSIDto dto = new CalculoRebateMSIDto();
		dto.setIdCalculoRebate(arrObj[INDEX_ID_CALCULO_REBATE] != null ? Integer.valueOf(arrObj[INDEX_ID_CALCULO_REBATE].toString()) : null);
		dto.setOrigen(arrObj[INDEX_ORIGEN] != null ? arrObj[INDEX_ORIGEN].toString() : null);
		dto.setMonedaVenta(arrObj[INDEX_MONEDA_VENTA] != null ? arrObj[INDEX_MONEDA_VENTA].toString() : null);
		dto.setRfc(arrObj[INDEX_RFC] != null ? arrObj[INDEX_RFC].toString() : null);
		dto.setNumeroProveedor(arrObj[INDEX_NUMERO_PROVEEDOR] != null ? arrObj[INDEX_NUMERO_PROVEEDOR].toString() : null);
		dto.setFamilia(arrObj[INDEX_FAMILIA] != null ? arrObj[INDEX_FAMILIA].toString() : null);
		dto.setNombreFamilia(arrObj[INDEX_NOMBRE_FAMILIA] != null ? arrObj[INDEX_NOMBRE_FAMILIA].toString() : null);
		dto.setTicketVenta(arrObj[INDEX_TICKET_VENTA] != null ? arrObj[INDEX_TICKET_VENTA].toString() : null);
		dto.setSucursalVenta(arrObj[INDEX_SUCURSAL_VENTA] != null ? arrObj[INDEX_SUCURSAL_VENTA].toString() : null);
		dto.setFechaVenta(arrObj[INDEX_FECHA_VENTA] != null ? sdf.parse( arrObj[INDEX_FECHA_VENTA].toString() ) : null);
		dto.setBanco(arrObj[INDEX_BANCO] != null ? arrObj[INDEX_BANCO].toString() : null);
		dto.setNumCuota(arrObj[INDEX_NUM_CUOTA] != null ? Integer.valueOf(arrObj[INDEX_NUM_CUOTA].toString()) : null);
		dto.setSku(arrObj[INDEX_SKU] != null ? arrObj[INDEX_SKU].toString() : null);
		dto.setDescripcionProducto(arrObj[INDEX_DESCRIPCION_PRODUCTO] != null ? arrObj[INDEX_DESCRIPCION_PRODUCTO].toString() : null);
		dto.setSubtotalSku(arrObj[INDEX_SUBTOTAL_SKU] != null ? Double.valueOf( arrObj[INDEX_SUBTOTAL_SKU].toString() ) : null);
		dto.setMontoVentaSku(arrObj[INDEX_MONTO_VENTA_SKU] != null ? Double.valueOf( arrObj[INDEX_MONTO_VENTA_SKU].toString() ) : null);
		dto.setTipoAcuerdo(arrObj[INDEX_TIPO_ACUERDO] != null ? arrObj[INDEX_TIPO_ACUERDO].toString() : null);
		dto.setMonedaAcuerdo(arrObj[INDEX_MONEDA_ACUERDO] != null ? arrObj[INDEX_MONEDA_ACUERDO].toString() : null);
		dto.setValorDescuento(arrObj[INDEX_VALOR_DESCUENTO] != null ? Double.valueOf( arrObj[INDEX_VALOR_DESCUENTO].toString() ) : null);
		dto.setTipoDescuento(arrObj[INDEX_TIPO_DESCUENTO] != null ? arrObj[INDEX_TIPO_DESCUENTO].toString() : null);
		dto.setMontoRebate(arrObj[INDEX_MONTO_REBATE] != null ? Double.valueOf(arrObj[INDEX_MONTO_REBATE].toString()) : null);
		dto.setIvaRebate(arrObj[INDEX_IVA_REBATE] != null ? Double.valueOf( arrObj[INDEX_IVA_REBATE].toString() ) : null);
		dto.setMontoTotalRebate(arrObj[INDEX_MONTO_TOTAL_REBATE] != null ? Double.valueOf( arrObj[INDEX_MONTO_TOTAL_REBATE].toString()) : null);
		dto.setProgramaPago(arrObj[INDEX_PROGRAMA_PAGO] != null ? arrObj[INDEX_PROGRAMA_PAGO].toString() : null);
		dto.setIdPeriodo(arrObj[INDEX_ID_PERIODO] != null ? Integer.valueOf(arrObj[INDEX_ID_PERIODO].toString()) : null);
		dto.setSubtotalCuenta(arrObj[INDEX_SUBTOTAL_CUENTA] != null ? arrObj[INDEX_SUBTOTAL_CUENTA].toString() : null);
		dto.setIvaCuenta(arrObj[INDEX_IVA_CUENTA] != null ? arrObj[INDEX_IVA_CUENTA].toString() : null);
		dto.setProveedorMercancia(arrObj[INDEX_PROVEEDOR_MERCANCIA] != null ? arrObj[INDEX_PROVEEDOR_MERCANCIA].toString() : null);
		dto.setTipoDocumentoPoliza(arrObj[INDEX_TIPO_DOCUMENTO_POLIZA] != null ? arrObj[INDEX_TIPO_DOCUMENTO_POLIZA].toString() : null);
		dto.setCentroCostos(arrObj[INDEX_CENTRO_COSTOS] != null ? arrObj[INDEX_CENTRO_COSTOS].toString() : null);
		dto.setCentroBeneficios(arrObj[INDEX_CENTRO_BENEFICIOS] != null ? arrObj[INDEX_CENTRO_BENEFICIOS].toString() : null);
		dto.setSucursal(arrObj[INDEX_SUCURSAL] != null ? Integer.valueOf(arrObj[INDEX_SUCURSAL].toString()) : null);
		dto.setCondicionesPago(arrObj[INDEX_CONDICIONES_PAGO] != null ? arrObj[INDEX_CONDICIONES_PAGO].toString() : null);
		dto.setExclusion(arrObj[INDEX_EXCLUSION] != null ? Integer.valueOf(arrObj[INDEX_EXCLUSION].toString()) : null);
		dto.setFechaExclusion(arrObj[INDEX_FECHA_EXCLUSION] != null ? sdf2.parse( arrObj[INDEX_FECHA_EXCLUSION].toString() ) : null);
		dto.setIdExclusion(arrObj[INDEX_ID_EXCLUSION] != null ? Integer.valueOf(arrObj[INDEX_ID_EXCLUSION].toString()) : null);
		return dto;
	}

	public static List<CalculoRebateMSIDto> convertToDtos(List<Object[]> resultList) throws ParseException {
		 List<CalculoRebateMSIDto> dtos = new ArrayList<>();
		 if (resultList != null) {
			 for (Object[] arrObj : resultList ) {
				 dtos.add( convertToDto(arrObj) );
			 }
		 }
		return dtos;
	}
	
	
}

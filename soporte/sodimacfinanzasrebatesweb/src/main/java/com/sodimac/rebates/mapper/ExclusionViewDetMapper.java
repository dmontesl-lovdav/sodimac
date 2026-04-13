package com.sodimac.rebates.mapper;

import java.util.ArrayList;
import java.util.List;

import com.sodimac.rebates.dto.ExclusionViewDetDto;

public class ExclusionViewDetMapper {
	
	private static final int INDEX_ID_CAT_PERIODO = 0;
	private static final int INDEX_DETALLE_PERIODO = 1;
	//private static final int INDEX_PERIODO_FECHA_INI = 2;
	//private static final int INDEX_PERIODO_FECHA_FIN = 3;
	private static final int INDEX_ID_EXCLUSION = 4;
	private static final int INDEX_ID_CAT_TIPOREBATE = 5;
	private static final int INDEX_DESCRIPCION_REBATE = 6;
	private static final int INDEX_ID_CAT_TIPO_EXCLUSION = 7;
	private static final int INDEX_DESCRIPCION_EXCLUSION = 8;
	private static final int INDEX_ID_CAT_ESTATUS_EXCLUSION = 9;
	private static final int INDEX_FOLIO = 10;
	private static final int INDEX_CONTABILIZADO = 11;
	private static final int INDEX_COMENTARIO = 12;
	private static final int INDEX_ID_EXCLUSION_CARGA = 13;
	private static final int INDEX_ID_EXCLUSION_CARGA_DET = 14;
	private static final int INDEX_MOTIVO = 15;
	private static final int INDEX_NUM_PROVEEDOR = 16;
	private static final int INDEX_NOM_PROVEEDOR = 17;
	private static final int INDEX_ORDEN_COMPRA = 18;
	private static final int INDEX_CLACOM = 19;
	private static final int INDEX_SKU = 20;
	private static final int INDEX_SKU_DESCRIPCION = 21;
	private static final int INDEX_PERIODO_VIGENTE = 23;
	private static final int INDEX_TIENE_ACUERDO   = 24;

	public static ExclusionViewDetDto convertDto(Object[] arrObj) {
		ExclusionViewDetDto dto = new ExclusionViewDetDto();
		dto.setIdCatPeriodo( (arrObj[INDEX_ID_CAT_PERIODO] != null) ? Integer.valueOf(arrObj[INDEX_ID_CAT_PERIODO].toString()) : null );
		dto.setDetallePeriodo( (arrObj[INDEX_DETALLE_PERIODO] != null) ? (arrObj[INDEX_DETALLE_PERIODO].toString()) : null );
		//dto.setPeriodoFechaIni( (arrObj[INDEX_PERIODO_FECHA_INI] != null) ? (arrObj[INDEX_PERIODO_FECHA_INI].toString()) : null );
		//dto.setPeriodoFechaFin( (arrObj[INDEX_PERIODO_FECHA_FIN] != null) ? (arrObj[INDEX_PERIODO_FECHA_FIN].toString()) : null );
		dto.setIdExclusion( (arrObj[INDEX_ID_EXCLUSION] != null) ? Integer.valueOf(arrObj[INDEX_ID_EXCLUSION].toString()) : null );
		dto.setIdCatTipoRebate( (arrObj[INDEX_ID_CAT_TIPOREBATE] != null) ? Integer.valueOf(arrObj[INDEX_ID_CAT_TIPOREBATE].toString()) : null );
		dto.setDescripcionRebate( (arrObj[INDEX_DESCRIPCION_REBATE] != null) ? (arrObj[INDEX_DESCRIPCION_REBATE].toString()) : null );
		dto.setIdCatTipoExclusion( (arrObj[INDEX_ID_CAT_TIPO_EXCLUSION] != null) ? Integer.valueOf(arrObj[INDEX_ID_CAT_TIPO_EXCLUSION].toString()) : null );
		dto.setDescripcionExclusion( (arrObj[INDEX_DESCRIPCION_EXCLUSION] != null) ? (arrObj[INDEX_DESCRIPCION_EXCLUSION].toString()) : null );
		dto.setIdCatEstatusExclusion( (arrObj[INDEX_ID_CAT_ESTATUS_EXCLUSION] != null) ? Integer.valueOf(arrObj[INDEX_ID_CAT_ESTATUS_EXCLUSION].toString()) : null );
		dto.setFolio( (arrObj[INDEX_FOLIO] != null) ? (arrObj[INDEX_FOLIO].toString()) : null );
		dto.setContabilizado( (arrObj[INDEX_CONTABILIZADO] != null) ? Integer.valueOf(arrObj[INDEX_CONTABILIZADO].toString()) : null );
		dto.setComentario( (arrObj[INDEX_COMENTARIO] != null) ? (arrObj[INDEX_COMENTARIO].toString()) : null );
		dto.setIdExclusionCarga( (arrObj[INDEX_ID_EXCLUSION_CARGA] != null) ? Integer.valueOf(arrObj[INDEX_ID_EXCLUSION_CARGA].toString()) : null );
		dto.setIdExclusionCargaDet( (arrObj[INDEX_ID_EXCLUSION_CARGA_DET] != null) ? Integer.valueOf(arrObj[INDEX_ID_EXCLUSION_CARGA_DET].toString()) : null );
		dto.setMotivo( (arrObj[INDEX_MOTIVO] != null) ? (arrObj[INDEX_MOTIVO].toString()) : null );
		dto.setNumProveedor( (arrObj[INDEX_NUM_PROVEEDOR] != null) ? (arrObj[INDEX_NUM_PROVEEDOR].toString()) : null );
		dto.setNomProveedor( (arrObj[INDEX_NOM_PROVEEDOR] != null) ? (arrObj[INDEX_NOM_PROVEEDOR].toString()) : null );
		dto.setOrdenCompra( (arrObj[INDEX_ORDEN_COMPRA] != null) ? (arrObj[INDEX_ORDEN_COMPRA].toString()) : null );
		dto.setClacom( (arrObj[INDEX_CLACOM] != null) ? (arrObj[INDEX_CLACOM].toString()) : null );
		dto.setSku( (arrObj[INDEX_SKU] != null) ? (arrObj[INDEX_SKU].toString()) : null );
		dto.setSkuDescripcion( (arrObj[INDEX_SKU_DESCRIPCION] != null) ? (arrObj[INDEX_SKU_DESCRIPCION].toString()) : null );
		dto.setPeriodoVigente( (arrObj[INDEX_PERIODO_VIGENTE] != null) ? Integer.valueOf(arrObj[INDEX_PERIODO_VIGENTE].toString()) : null );
		dto.setTieneAcuerdo(arrObj[INDEX_TIENE_ACUERDO] != null ? Boolean.valueOf(arrObj[INDEX_TIENE_ACUERDO].toString()) : null);
		return dto;
	}

	public static List<ExclusionViewDetDto> convertDtos(List<Object[]> resultList) {
		List<ExclusionViewDetDto> dtos = new ArrayList<>();
		if (resultList != null) {
			for (Object[] arrObj : resultList) {
				dtos.add( convertDto(arrObj) );
			}
		}
		return dtos;
	}

}

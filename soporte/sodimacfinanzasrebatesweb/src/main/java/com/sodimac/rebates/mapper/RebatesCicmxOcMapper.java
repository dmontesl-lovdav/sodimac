package com.sodimac.rebates.mapper;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;

import com.sodimac.rebates.dto.RebatesCicmxOcDto;

public final class RebatesCicmxOcMapper {
	
	private static SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
	
	private static int PROVEEDOR_NUM_PROVEEDOR = 0;
	private static int PROVEEDOR_NOM_PROVEEDOR = 1;
	
	private static int OC_NUM_PROVEEDOR = 0;
	private static int OC_NOM_PROVEEDOR = 1;
	private static int OC_NUM_OC = 2;
	private static int FECHA_RECEPCION = 3;
	
	
	private static int FAM_NUM_PROVEEDOR = 0;
	private static int FAM_NOM_PROVEEDOR = 1;
	private static int FAM_CLACOM = 2;
	private static int FAM_TOTAL = 3;
	
	private static int FAM_UNICA_CLACOM = 0;
	
	private static int SKU_NUM_OC = 0;
	private static int SKU_NUM_PROVEEDOR = 1;
	private static int SKU_NOM_PROVEEDOR = 2;
	private static int SKU_CLACOM = 3;
	private static int SKU_SKU = 4;
	private static int SKU_DESCRIPCION = 5;
	private static int SKU_TOTAL = 6;

	private static int SKU_UNICO_CLACOM = 0;
	private static int SKU_UNICO_SKU = 1;
	private static int SKU_UNICO_DESCRIPCION = 2;

	public static List<RebatesCicmxOcDto> convertDtoProveedores(List<Object[]> listObj) {
		List<RebatesCicmxOcDto> listDto = new ArrayList<>();
		if (listObj != null && listObj.size() > 0) {
			for (Object[] arrObj : listObj) {
				RebatesCicmxOcDto dto = new RebatesCicmxOcDto();
				dto.setNumProveedor( arrObj[PROVEEDOR_NUM_PROVEEDOR] != null ? Integer.valueOf(arrObj[PROVEEDOR_NUM_PROVEEDOR].toString()) : null );
				dto.setNomProveedor( arrObj[PROVEEDOR_NOM_PROVEEDOR] != null ? arrObj[PROVEEDOR_NOM_PROVEEDOR].toString() : null );
				
				StringBuilder jsonId = new StringBuilder();
				jsonId.append("numProveedor=").append(dto.getNumProveedor()).append("|");
				dto.setJsonId(jsonId.toString());
				
				listDto.add(dto);
			}
		}
		return listDto;
	}
	
	public static List<RebatesCicmxOcDto> convertDtoOrdenCompra(List<Object[]> listObj) throws ParseException {
		List<RebatesCicmxOcDto> listDto = new ArrayList<>();
		if (listObj != null && listObj.size() > 0) {
			for (Object[] arrObj : listObj) {
				RebatesCicmxOcDto dto = new RebatesCicmxOcDto();
				dto.setNumProveedor( arrObj[OC_NUM_PROVEEDOR] != null ? Integer.valueOf(arrObj[OC_NUM_PROVEEDOR].toString()) : null );
				dto.setNomProveedor( arrObj[OC_NOM_PROVEEDOR] != null ? arrObj[OC_NOM_PROVEEDOR].toString() : null );
				dto.setNumOc( arrObj[OC_NUM_OC] != null ? Integer.valueOf(arrObj[OC_NUM_OC].toString()) : null );
				dto.setFecRecepcion( arrObj[FECHA_RECEPCION] != null ? sdf.parse(arrObj[FECHA_RECEPCION].toString()) : null );
				
				StringBuilder jsonId = new StringBuilder();
				jsonId.append("numProveedor=").append(dto.getNumProveedor()).append("#")
					  .append("numOc=").append(dto.getNumOc()).append("|");
				
				dto.setJsonId(jsonId.toString());
				
				listDto.add(dto);
			}
		}
		return listDto;
	}
	
	public static List<RebatesCicmxOcDto> convertDtoFamilia(List<Object[]> listObj) {
		List<RebatesCicmxOcDto> listDto = new ArrayList<>();
		if (listObj != null && listObj.size() > 0) {
			for (Object[] arrObj : listObj) {
				RebatesCicmxOcDto dto = new RebatesCicmxOcDto();
				//dto.setNumOc( arrObj[FAM_NUM_OC] != null ? Integer.valueOf(arrObj[FAM_NUM_OC].toString()) : null );
				dto.setNumProveedor( arrObj[FAM_NUM_PROVEEDOR] != null ? Integer.valueOf(arrObj[FAM_NUM_PROVEEDOR].toString()) : null );
				dto.setNomProveedor( arrObj[FAM_NOM_PROVEEDOR] != null ? (arrObj[FAM_NOM_PROVEEDOR].toString()) : null );
				dto.setClacom( arrObj[FAM_CLACOM] != null ? (arrObj[FAM_CLACOM].toString()) : null );
				dto.setTotal( arrObj[FAM_TOTAL] != null ? Integer.valueOf(arrObj[FAM_TOTAL].toString()) : null );
				
				StringBuilder jsonId = new StringBuilder();
				jsonId.append("numProveedor=").append(dto.getNumProveedor()).append("#")
					  .append("clacom=").append(dto.getClacom()).append("|");
				
				dto.setJsonId(jsonId.toString());
				
				listDto.add(dto);
			}
		}
		return listDto;
	}
	
	public static List<RebatesCicmxOcDto> convertDtoFamiliaUnica(List<Object[]> listObj) {
		List<RebatesCicmxOcDto> listDto = new ArrayList<>();
		if (listObj != null && listObj.size() > 0) {
			for (Object[] arrObj : listObj) {
				RebatesCicmxOcDto dto = new RebatesCicmxOcDto();
				dto.setClacom( arrObj[FAM_UNICA_CLACOM] != null ? (arrObj[FAM_UNICA_CLACOM].toString()) : null );
				listDto.add(dto);
			}
		}
		return listDto;
	}
	
	public static List<RebatesCicmxOcDto> convertDtoSku(List<Object[]> listObj) {
		List<RebatesCicmxOcDto> listDto = new ArrayList<>();
		if (listObj != null && listObj.size() > 0) {
			for (Object[] arrObj : listObj) {
				RebatesCicmxOcDto dto = new RebatesCicmxOcDto();
				dto.setNumOc( arrObj[SKU_NUM_OC] != null ? Integer.valueOf(arrObj[SKU_NUM_OC].toString()) : null );
				dto.setNumProveedor( arrObj[SKU_NUM_PROVEEDOR] != null ? Integer.valueOf(arrObj[SKU_NUM_PROVEEDOR].toString()) : null );
				dto.setNomProveedor( arrObj[SKU_NOM_PROVEEDOR] != null ? (arrObj[SKU_NOM_PROVEEDOR].toString()) : null );
				dto.setClacom( arrObj[SKU_CLACOM] != null ? (arrObj[SKU_CLACOM].toString()) : null );
				dto.setSku( arrObj[SKU_SKU] != null ? (arrObj[SKU_SKU].toString()) : null );
				dto.setSkuDescripcion( arrObj[SKU_DESCRIPCION] != null ? (arrObj[SKU_DESCRIPCION].toString()) : null );
				dto.setTotal( arrObj[SKU_TOTAL] != null ? Integer.valueOf(arrObj[SKU_TOTAL].toString()) : null );
				dto.setChecked(0);
				
				StringBuilder jsonId = new StringBuilder();
				jsonId.append("numProveedor=").append(dto.getNumProveedor()).append("#")
					  .append("clacom=").append(dto.getClacom()).append("#")
				      .append("numOc=").append(dto.getNumOc()).append("#")
				      .append("sku=").append(dto.getSku()).append("|");
				
				dto.setJsonId(jsonId.toString());
				listDto.add(dto);
			}
		}
		return listDto;
	}
	
	public static List<RebatesCicmxOcDto> convertDtoSkuUnico(List<Object[]> listObj) {
		List<RebatesCicmxOcDto> listDto = new ArrayList<>();
		if (listObj != null && listObj.size() > 0) {
			for (Object[] arrObj : listObj) {
				RebatesCicmxOcDto dto = new RebatesCicmxOcDto();
				dto.setSku( arrObj[SKU_UNICO_CLACOM] != null ? (arrObj[SKU_UNICO_CLACOM].toString()) : null );
				dto.setSku( arrObj[SKU_UNICO_SKU] != null ? (arrObj[SKU_UNICO_SKU].toString()) : null );
				dto.setSkuDescripcion( arrObj[SKU_UNICO_DESCRIPCION] != null ? (arrObj[SKU_UNICO_DESCRIPCION].toString()) : null );
				dto.setChecked(0);
				listDto.add(dto);
			}
		}
		return listDto;
	}
	

	public static List<RebatesCicmxOcDto> convertDtoSkuDet(List<Object[]> listObj) {
		List<RebatesCicmxOcDto> listDto = new ArrayList<>();
		if (listObj != null && listObj.size() > 0) {
			for (Object[] arrObj : listObj) {
				RebatesCicmxOcDto dto = new RebatesCicmxOcDto();
				dto.setNumOc( arrObj[SKU_NUM_OC] != null ? Integer.valueOf(arrObj[SKU_NUM_OC].toString()) : null );
				dto.setNumProveedor( arrObj[SKU_NUM_PROVEEDOR] != null ? Integer.valueOf(arrObj[SKU_NUM_PROVEEDOR].toString()) : null );
				dto.setNomProveedor( arrObj[SKU_NOM_PROVEEDOR] != null ? (arrObj[SKU_NOM_PROVEEDOR].toString()) : null );
				dto.setClacom( arrObj[SKU_CLACOM] != null ? (arrObj[SKU_CLACOM].toString()) : null );
				dto.setSku( arrObj[SKU_SKU] != null ? (arrObj[SKU_SKU].toString()) : null );
				dto.setSkuDescripcion( arrObj[SKU_DESCRIPCION] != null ? (arrObj[SKU_DESCRIPCION].toString()) : null );
				dto.setTotal( arrObj[SKU_TOTAL] != null ? Integer.valueOf(arrObj[SKU_TOTAL].toString()) : null );
				//dto.setChecked(0);
				//dto.setChecked(1);
				
				listDto.add(dto);
			}
		}
		return listDto;
	}
}

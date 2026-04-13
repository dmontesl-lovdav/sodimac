package com.sodimac.rebates.mapper;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;

import com.sodimac.rebates.dto.ExclusionCargaDetDto;

public class ExclusionCargaDetMapper {

	private static int PROVEEDOR_PROVEEDOR = 0;
	private static int PROVEEDOR_NOMBRE = 1;
	
	private static int OC_NUM_PROVEEDOR = 0;
	private static int OC_NOM_PROVEEDOR = 1;
	private static int OC_NUM_OC = 2;
	
	private static int FAM_NUM_PROVEEDOR = 0;
	private static int FAM_NOM_PROVEEDOR = 1;
	private static int FAM_NUM_OC = 2;
	private static int FAM_CLACOM = 4;
	
	private static int SKU_ID_EXCLUSION_CARGA_DET = 0;
	private static int SKU_ID_EXCLUSION_CARGA = 1;
	private static int SKU_NUM_PROVEEDOR = 2;
	private static int SKU_NOM_PROVEEDOR = 3;
	private static int SKU_NUM_OC = 4;
	private static int SKU_CLACOM = 5;
	private static int SKU_SKU = 6;
	private static int SKU_DESCRIPCION = 7;
	private static int SKU_MOTIVO = 9;
	private static int SKU_CANTIDAD_ORDENADA = 10;
	private static int SKU_CANTIDAD_RECIBIDA = 11;
	
	private static int SKU_UNICO_SKU = 0;
	private static int SKU_UNICO_DESCRIPCION = 1;
	
	private static final String NUM_PROVEEDOR = "numProveedor";
	private static final String NUM_OC = "numOc";
	private static final String CLACOM = "clacom";
	private static final String SKU = "sku";
	
	public static List<ExclusionCargaDetDto> convertDtoProveedores(List<Object[]> listObj) {
		List<ExclusionCargaDetDto> listDto = new ArrayList<>();
		if (listObj != null && listObj.size() > 0) {
			for (Object[] arrObj : listObj) {
				ExclusionCargaDetDto dto = new ExclusionCargaDetDto();
				dto.setNumProveedor( arrObj[PROVEEDOR_PROVEEDOR] != null ? arrObj[PROVEEDOR_PROVEEDOR].toString() : null );
				dto.setNomProveedor( arrObj[PROVEEDOR_PROVEEDOR] != null ? arrObj[PROVEEDOR_NOMBRE].toString() : null );
				listDto.add(dto);
			}
		}
		return listDto;
	}

	public static List<ExclusionCargaDetDto> convertDtoOrdenCompra(List<Object[]> listObj) {
		List<ExclusionCargaDetDto> listDto = new ArrayList<>();
		if (listObj != null && listObj.size() > 0) {
			for (Object[] arrObj : listObj) {
				ExclusionCargaDetDto dto = new ExclusionCargaDetDto();
				dto.setNumProveedor( arrObj[OC_NUM_PROVEEDOR] != null ? arrObj[OC_NUM_PROVEEDOR].toString() : null );
				dto.setNomProveedor( arrObj[OC_NOM_PROVEEDOR] != null ? arrObj[OC_NOM_PROVEEDOR].toString() : null );
				dto.setOrdenCompra( arrObj[OC_NUM_OC] != null ? arrObj[OC_NUM_OC].toString() : null );
				listDto.add(dto);
			}
		}
		return listDto;
	}

	public static List<ExclusionCargaDetDto> convertDtoFamilia(List<Object[]> listObj) {
		List<ExclusionCargaDetDto> listDto = new ArrayList<>();
		if (listObj != null && listObj.size() > 0) {
			for (Object[] arrObj : listObj) {
				ExclusionCargaDetDto dto = new ExclusionCargaDetDto();
				dto.setNumProveedor( arrObj[FAM_NUM_PROVEEDOR] != null ? arrObj[FAM_NUM_PROVEEDOR].toString() : null );
				dto.setNomProveedor( arrObj[FAM_NOM_PROVEEDOR] != null ? arrObj[FAM_NOM_PROVEEDOR].toString() : null );
				dto.setOrdenCompra( arrObj[FAM_NUM_OC] != null ? arrObj[FAM_NUM_OC].toString() : null );
				dto.setClacom( arrObj[FAM_CLACOM] != null ? (arrObj[FAM_CLACOM].toString()) : null );
				listDto.add(dto);
			}
		}
		return listDto;
	}

	public static List<ExclusionCargaDetDto> convertDtoSku(List<Object[]> listObj) {
		List<ExclusionCargaDetDto> listDto = new ArrayList<>();
		if (listObj != null && listObj.size() > 0) {
			for (Object[] arrObj : listObj) {
				ExclusionCargaDetDto dto = new ExclusionCargaDetDto();
				dto.setIdExclusionCargaDet(arrObj[SKU_ID_EXCLUSION_CARGA_DET] != null ? Long.valueOf(arrObj[SKU_ID_EXCLUSION_CARGA_DET].toString()) : null);
				dto.setIdExclusionCarga(arrObj[SKU_ID_EXCLUSION_CARGA] != null ? Integer.valueOf(arrObj[SKU_ID_EXCLUSION_CARGA].toString()) : null);
				dto.setNumProveedor( arrObj[SKU_NUM_PROVEEDOR] != null ? arrObj[SKU_NUM_PROVEEDOR].toString() : null );
				dto.setNomProveedor( arrObj[SKU_NOM_PROVEEDOR] != null ? arrObj[SKU_NOM_PROVEEDOR].toString() : null );
				dto.setOrdenCompra( arrObj[SKU_NUM_OC] != null ? arrObj[SKU_NUM_OC].toString() : null );
				dto.setClacom( arrObj[SKU_CLACOM] != null ? (arrObj[SKU_CLACOM].toString()) : null );
				dto.setSku( arrObj[SKU_SKU] != null ? (arrObj[SKU_SKU].toString()) : null );
				dto.setSkuDescripcion( arrObj[SKU_DESCRIPCION] != null ? (arrObj[SKU_DESCRIPCION].toString()) : null );
				dto.setMotivo( arrObj[SKU_MOTIVO] != null ? (arrObj[SKU_MOTIVO].toString()) : null );
				dto.setCantidadOrdenada( arrObj[SKU_CANTIDAD_ORDENADA] != null ? (arrObj[SKU_CANTIDAD_ORDENADA].toString()) : null );
				dto.setCantidadRecibida( arrObj[SKU_CANTIDAD_RECIBIDA] != null ? (arrObj[SKU_CANTIDAD_RECIBIDA].toString()) : null );
				listDto.add(dto);
			}
		}
		return listDto;
	}
	
	public static List<ExclusionCargaDetDto> convertDtoSkuUnicos(List<Object[]> listObj) {
		List<ExclusionCargaDetDto> listDto = new ArrayList<>();
		if (listObj != null && listObj.size() > 0) {
			for (Object[] arrObj : listObj) {
				ExclusionCargaDetDto dto = new ExclusionCargaDetDto();
				dto.setSku( arrObj[SKU_UNICO_SKU] != null ? (arrObj[SKU_UNICO_SKU].toString()) : null );
				dto.setSkuDescripcion( arrObj[SKU_UNICO_DESCRIPCION] != null ? (arrObj[SKU_UNICO_DESCRIPCION].toString()) : null );
				listDto.add(dto);
			}
		}
		return listDto;
	}
	
	public static List<ExclusionCargaDetDto> convertJson(String jsonId) {
		List<ExclusionCargaDetDto> listExclusionCargaDet = new ArrayList<>();
		
		if (jsonId.indexOf("|") > 0) {
			String[] numRegistro = jsonId.split(Pattern.quote("|"));
			if (numRegistro != null && numRegistro.length > 0) {
				for (String registro : numRegistro) {
					
					ExclusionCargaDetDto cargaDetDto = new ExclusionCargaDetDto();
					
					String numProveedor = null;
					String numOc = null;
					String clacom = null;
					String sku = null;
					
					if (registro.indexOf("#") > -1) {
						String[] atributos = registro.split(Pattern.quote("#"));
						if (atributos != null && atributos.length > 0) {
							
							siguienteAtt:
							for (String atributo : atributos) {
								if (atributo.contains(NUM_PROVEEDOR)) {
									numProveedor = getValueAttribute(atributo, NUM_PROVEEDOR);
									continue siguienteAtt;
								} else if (atributo.contains(NUM_OC)) {
									numOc = getValueAttribute(atributo, NUM_OC);
									continue siguienteAtt;
								} else if (atributo.contains(CLACOM)) {
									clacom = getValueAttribute(atributo, CLACOM);
									continue siguienteAtt;
								} else if (atributo.contains(SKU)) {
									sku = getValueAttribute(atributo, SKU);
									continue siguienteAtt;
								}
							}//for (String atributo : atributos) {
						}//if (atributos != null && atributos.length > 0)
					} else {
						if (registro.contains(NUM_PROVEEDOR)) {
							numProveedor = getValueAttribute(registro, NUM_PROVEEDOR);
						} else if (registro.contains(NUM_PROVEEDOR)) {
							numOc = getValueAttribute(registro, NUM_OC);
						} else if (registro.contains(NUM_PROVEEDOR)) {
							clacom = getValueAttribute(registro, CLACOM);
						} else if (registro.contains(NUM_PROVEEDOR)) {
							sku = getValueAttribute(registro, SKU);
						}
						
					} //if (registro.indexOf("#") > -1) {
					
					cargaDetDto.setNumProveedor(numProveedor);
					cargaDetDto.setOrdenCompra(numOc);
					cargaDetDto.setClacom(clacom);
					cargaDetDto.setSku(sku);
					
					listExclusionCargaDet.add(cargaDetDto);
					
				}//for (String registro : numRegistro)
			} //if (numRegistro != null && numRegistro.length > 0)
		} //if (jsonId.indexOf("|") > 0) {
		return listExclusionCargaDet;
	}
	
	private static String getValueAttribute(String value, String attribute) {
		if (value.contains(attribute)) {
			String[] attributeValue = value.split(Pattern.quote("="));
			if (attributeValue != null && attributeValue.length >= 1) {
				//clave=valor
				return attributeValue[1];
			}
		}
		return null;
	}
}

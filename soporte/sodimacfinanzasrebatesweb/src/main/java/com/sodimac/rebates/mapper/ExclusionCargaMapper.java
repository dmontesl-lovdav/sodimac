package com.sodimac.rebates.mapper;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import com.sodimac.rebates.dto.ExclusionCargaDto;
import com.sodimac.rebates.model.entity.ExclusionCargaEntity;

public final class ExclusionCargaMapper {

	private static SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
	private static int INDEX_ID_EXCLUSION_CARGA = 0;
	private static int INDEX_ID_EXCLUSION 		= 1;
	private static int INDEX_CARGA 				= 2;
	private static int INDEX_MOTIVO 			= 3;
	private static int INDEX_NUM_PROVEEDOR 		= 4;
	private static int INDEX_NOM_PROVEEDOR 		= 5;
	private static int INDEX_PERIODO_VIGENTE	= 6;
	private static int INDEX_FECHA_RECEPCION	= 7;
	private static int INDEX_TIENE_ACUERDO		= 8;
	
	
	public static List<ExclusionCargaDto> convertDtosObj(List<Object[]> listObj) throws ParseException {
		List<ExclusionCargaDto> listDtos = new ArrayList<>();
		if (listObj != null) {
			for (Object[] arrObj : listObj) {
				listDtos.add( ExclusionCargaMapper.convertDto(arrObj) );
			}
		}
		return listDtos;
	}
	
	public static ExclusionCargaDto convertDto(Object[] arrObj) throws ParseException {
		ExclusionCargaDto dto = new ExclusionCargaDto();
		dto.setIdExclusionCarga(arrObj[INDEX_ID_EXCLUSION_CARGA] != null ? Long.valueOf(arrObj[INDEX_ID_EXCLUSION_CARGA].toString()) : null);
		dto.setIdExclusion(arrObj[INDEX_ID_EXCLUSION] != null ? Integer.valueOf(arrObj[INDEX_ID_EXCLUSION].toString()) : null);
		dto.setCarga(arrObj[INDEX_CARGA] != null ? arrObj[INDEX_CARGA].toString() : null);
		dto.setMotivo( arrObj[INDEX_MOTIVO] != null ? arrObj[INDEX_MOTIVO].toString() : null );
		dto.setNumProveedor( arrObj[INDEX_NUM_PROVEEDOR] != null ? arrObj[INDEX_NUM_PROVEEDOR].toString() : null );
		dto.setNomProveedor( arrObj[INDEX_NOM_PROVEEDOR] != null ? arrObj[INDEX_NOM_PROVEEDOR].toString() : null );
		dto.setPeriodoVigente(arrObj[INDEX_PERIODO_VIGENTE] != null ? Integer.valueOf(arrObj[INDEX_PERIODO_VIGENTE].toString()) : null);
		dto.setFechaRecepcion(arrObj[INDEX_FECHA_RECEPCION] != null ? sdf.parse( arrObj[INDEX_FECHA_RECEPCION].toString() ) : null);
		dto.setTieneAcuerdo(arrObj[INDEX_TIENE_ACUERDO] != null ? Boolean.valueOf(arrObj[INDEX_TIENE_ACUERDO].toString()) : null);
		return dto;
	}
	
	public static ExclusionCargaDto convertDto(ExclusionCargaEntity entity) {
		ExclusionCargaDto dto = new ExclusionCargaDto();
		dto.setIdExclusionCarga(entity.getIdExclusionCarga());
		dto.setIdExclusion(entity.getIdExclusion());
		dto.setCarga(entity.getCarga());
		dto.setMotivo( entity.getMotivo() );
		dto.setFechaRegistro( entity.getFechaRegistro() );
		dto.setActivo( entity.isActivo() );
		return dto;
	}
	
	public static ExclusionCargaEntity convertEntity(ExclusionCargaDto dto) {
		ExclusionCargaEntity entity = new ExclusionCargaEntity();
		entity.setIdExclusionCarga(dto.getIdExclusionCarga());
		entity.setIdExclusion(dto.getIdExclusion());
		entity.setCarga(dto.getCarga());
		entity.setMotivo( dto.getMotivo() );
		entity.setFechaRegistro( new Date() );
		entity.setActivo( dto.isActivo() );
		return entity;
	}

	public static List<ExclusionCargaDto> convertDtos(List<ExclusionCargaEntity> listEntities) {
		List<ExclusionCargaDto> listDtos = new ArrayList<>();
		if (listEntities != null) {
			for (ExclusionCargaEntity entity : listEntities) {
				listDtos.add( ExclusionCargaMapper.convertDto(entity) );
			}
		}
		return listDtos;
	}	
}

package com.sodimac.rebates.mapper;

import java.util.ArrayList;
import java.util.List;

import com.sodimac.rebates.dto.PolizaContableDto;
import com.sodimac.rebates.model.PolizaContableEntity;

public final class PolizaContableMapper {
	
	private PolizaContableMapper() {
		super();
	}
	
	public static PolizaContableDto convertToDto(PolizaContableEntity entity) {
		PolizaContableDto dto = new PolizaContableDto();
		dto.setId( entity.getId() );
		dto.setCodigoProveedor( entity.getCodigoProveedor() );
		dto.setIdPeriodo( entity.getIdPeriodo() );
		dto.setFechaInicioPeriodo( entity.getFechaInicioPeriodo() );
		dto.setFechaFinPeriodo( entity.getFechaFinPeriodo() );
		dto.setIdTipoRebate( entity.getIdTipoRebate() );
		dto.setTipoRebate( entity.getTipoRebate() );
		dto.setMontoCalculado( entity.getMontoCalculado() );
		dto.setMontoPendiente( entity.getMontoPendiente() );
		dto.setMontoContabilizado( entity.getMontoContabilizado() );
		dto.setFechaContable( entity.getFechaContable() );
		dto.setFechaRecepcion( entity.getFechaRecepcion() );

		return dto;
	}
	
	public static List<PolizaContableDto> convertToDtos(List<PolizaContableEntity> entities) {
		List<PolizaContableDto> dtos = new ArrayList<>();
		if (entities != null) {
			for (PolizaContableEntity entity : entities) {
				dtos.add( convertToDto(entity) );
			}
		}
		return dtos;
		
	}
}

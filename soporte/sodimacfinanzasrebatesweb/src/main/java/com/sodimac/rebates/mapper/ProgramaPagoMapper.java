package com.sodimac.rebates.mapper;

import com.sodimac.rebates.dto.ProgramaPagoDto;
import com.sodimac.rebates.model.ProgramaPago;

public final class ProgramaPagoMapper {

	private ProgramaPagoMapper() {
		super();
	}
	
	public static ProgramaPagoDto convertToDto(ProgramaPago entity) {
		ProgramaPagoDto dto = new ProgramaPagoDto();
		dto.setIdCatProgramaPago( entity.getIdCatProgramaPago() );
		dto.setProgramaPago( entity.getProgramaPago() );
		dto.setNomenclatura( entity.getNomenclatura() );
		dto.setNumeroMeses( entity.getNumeroMeses() );
		dto.setActivo(entity.isActivo());
		return dto;
	}
	
	public static ProgramaPago convertToEntity(ProgramaPagoDto dto) {
		ProgramaPago entity = new ProgramaPago();
		entity.setIdCatProgramaPago( dto.getIdCatProgramaPago() );
		entity.setProgramaPago( dto.getProgramaPago() );
		entity.setNomenclatura( dto.getNomenclatura() );
		entity.setNumeroMeses( dto.getNumeroMeses() );
		entity.setActivo(dto.isActivo());
		return entity;
	}
}

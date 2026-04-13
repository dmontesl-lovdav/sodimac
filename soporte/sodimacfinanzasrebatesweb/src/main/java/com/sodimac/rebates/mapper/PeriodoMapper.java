package com.sodimac.rebates.mapper;

import java.util.ArrayList;
import java.util.List;

import com.sodimac.rebates.dto.PeriodoDto;
import com.sodimac.rebates.model.Periodo;

public final class PeriodoMapper {

	public static PeriodoDto convertDto(Periodo entity) {
		PeriodoDto dto = new PeriodoDto();
		dto.setIdCatPeriodo( entity.getIdCatPeriodo() );
		dto.setDetallePeriodo( entity.getDetallePeriodo() );
		dto.setEstatus( entity.getEstatus() );
		dto.setFechaIni( entity.getFechaIni() );
		dto.setFechaFin( entity.getFechaFin() );
		dto.setFechaEnvio( entity.getFechaEnvio() );
		dto.setActivo( entity.isActivo() );
		dto.setOrden( entity.getOrden() );
		return dto;
	}
	
	public static Periodo convertToEntity(PeriodoDto dto) {
		Periodo entity = new Periodo();
		entity.setIdCatPeriodo( dto.getIdCatPeriodo() );
		entity.setDetallePeriodo( dto.getDetallePeriodo() );
		entity.setEstatus( dto.getEstatus() );
		entity.setFechaIni( dto.getFechaIni() );
		entity.setFechaFin( dto.getFechaFin() );
		entity.setFechaEnvio( dto.getFechaEnvio() );
		entity.setActivo( dto.isActivo() );
		entity.setOrden( dto.getOrden() );
		return entity;
	}
	
	public static PeriodoDto convertToDtoSimple(Periodo entity) {
		PeriodoDto dto = new PeriodoDto();
		dto.setIdCatPeriodo( entity.getIdCatPeriodo() );
		return dto;
	}
	
	public static PeriodoDto convertDtoComplejo(Periodo entity) {
		PeriodoDto dto = new PeriodoDto();
		dto.setIdCatPeriodo( entity.getIdCatPeriodo() );
		dto.setDetallePeriodo( entity.getDetallePeriodo() );
		dto.setEstatus( entity.getEstatus() );
		dto.setFechaIni( entity.getFechaIni() );
		dto.setFechaFin( entity.getFechaFin() );
		dto.setFechaEnvio( entity.getFechaEnvio() );
		dto.setFechaCalculo(entity.getFechaCalculo());
		dto.setActivo( entity.isActivo() );
		dto.setOrden( entity.getOrden() );
		
		if (entity.getProgramaPago() != null) {
			dto.setProgramaPago( ProgramaPagoMapper.convertToDto(entity.getProgramaPago()) );
		}
		if (entity.getRelPeriodoTipoRebate() != null) {
			dto.setRelPeriodoTipoRebate( RelPeriodoTipoRebateMapper.convertToDtosSimple(entity.getRelPeriodoTipoRebate()) );
		}
		
		dto.setIdUsuarioCreacion(entity.getIdUsuarioCreacion());
		dto.setFechaHoraCreacion(entity.getFechaHoraCreacion());
		dto.setIdUsuarioModificacion(entity.getIdUsuarioModificacion());
		dto.setFechaHoraModificacion(entity.getFechaHoraModificacion());
		dto.setIdUsuarioModifEstatus(entity.getIdUsuarioModifEstatus());
		dto.setFechaHoraModifEstatus(entity.getFechaHoraModifEstatus());
		dto.setIdPerfil(entity.getIdPerfil());
		dto.setFechaHoraCierre(entity.getFechaHoraCierre());
		dto.setFechaHoraRespaldo(entity.getFechaHoraRespaldo());
		
		return dto;
	}

	public static Periodo convertEntity(PeriodoDto dto) {
		Periodo entity = new Periodo();
		entity.setIdCatPeriodo( dto.getIdCatPeriodo() );
		entity.setDetallePeriodo( dto.getDetallePeriodo() );
		if (dto.getProgramaPago() != null) {
			entity.setProgramaPago( ProgramaPagoMapper.convertToEntity( dto.getProgramaPago() ) );
		}
		entity.setFechaIni( dto.getFechaIni() );
		entity.setFechaFin( dto.getFechaFin() );
		entity.setFechaEnvio( dto.getFechaEnvio() );
		entity.setEstatus( dto.getEstatus() );
		entity.setActivo( dto.isActivo() );
		if (dto.getRelPeriodoTipoRebate() != null) {
			entity.setRelPeriodoTipoRebate( RelPeriodoTipoRebateMapper.convertToEntities( dto.getRelPeriodoTipoRebate() ) );
		}
		entity.setOrden(0);
		
		entity.setIdUsuarioCreacion(dto.getIdUsuarioCreacion());
		entity.setFechaHoraCreacion(dto.getFechaHoraCreacion());
		entity.setIdUsuarioModificacion(dto.getIdUsuarioModificacion());
		entity.setFechaHoraModificacion(dto.getFechaHoraModificacion());
		entity.setIdUsuarioModifEstatus(dto.getIdUsuarioModifEstatus());
		entity.setFechaHoraModifEstatus(dto.getFechaHoraModifEstatus());
		
		return entity;
	}

	public static List<PeriodoDto> convertDtos(List<Periodo> entities) {
		List<PeriodoDto> dtos = new ArrayList<>();
		if (entities != null) {
			for (Periodo periodo : entities) {
				dtos.add( convertDto(periodo) );
			}
		}
		return dtos;
	}

	public static List<PeriodoDto> convertDtosComplejo(List<Periodo> entities) {
		List<PeriodoDto> dtos = new ArrayList<>();
		if (entities != null) {
			for (Periodo periodo : entities) {
				dtos.add( convertDtoComplejo(periodo) );
			}
		}
		return dtos;
	}	
}

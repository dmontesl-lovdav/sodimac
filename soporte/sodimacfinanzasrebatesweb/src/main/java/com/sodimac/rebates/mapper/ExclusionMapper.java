package com.sodimac.rebates.mapper;

import java.util.ArrayList;
import java.util.List;

import com.sodimac.rebates.dto.CatEstatusExclusionDto;
import com.sodimac.rebates.dto.CatTipoExclusionDto;
import com.sodimac.rebates.dto.CatTipoRebateDto;
import com.sodimac.rebates.dto.ExclusionDto;
import com.sodimac.rebates.dto.PeriodoDto;
import com.sodimac.rebates.model.Periodo;
import com.sodimac.rebates.model.TipoRebate;
import com.sodimac.rebates.model.Usuario;
import com.sodimac.rebates.model.entity.CatEstatusExclusionEntity;
import com.sodimac.rebates.model.entity.CatTipoExclusionEntity;
import com.sodimac.rebates.model.entity.ExclusionEntity;

public final class ExclusionMapper {
	
	private static final int CONTABILIZADO = 1;
	private static final String CONTABILIZADO_SI = "Si";
	private static final String CONTABILIZADO_NO = "No";

	public static ExclusionDto convertDto(ExclusionEntity entity) {
		ExclusionDto dto = new ExclusionDto();
		dto.setIdExclusion( entity.getIdExclusion() );
		dto.setFolio( entity.getFolio() ) ;
		dto.setComentario( entity.getComentario() );
		dto.setUsuarioSolicitud( UsuarioMapper.convertDto( entity.getUsuarioSolicitud()) );
		dto.setUsuarioAutorizacion( UsuarioMapper.convertDto( entity.getUsuarioAutorizacion()) );
		dto.setFechaHoraSolicitud( entity.getFechaHoraSolicitud() );
		dto.setFechaHoraAutorizacion( entity.getFechaHoraAutorizacion() );
		dto.setActivo( entity.isActivo() );
		dto.setContabilizado( entity.getContabilizado() );
		if (entity.getContabilizado() != null && entity.getContabilizado().intValue() == CONTABILIZADO) {
			dto.setStrContabilizado(CONTABILIZADO_SI);
		} else {
			dto.setStrContabilizado(CONTABILIZADO_NO);
		}
				
		dto.setEvidencia( entity.getEvidencia() );
		if (entity.getEvidencia() != null && !entity.getEvidencia().isEmpty()) {
			dto.setImagenCargada(true);
		}
		
		if (entity.getPeriodo() != null) {
			PeriodoDto periodo = PeriodoMapper.convertDto(entity.getPeriodo());
			dto.setPeriodo(periodo);
		}
		if (entity.getCatTipoRebate() != null) {
			CatTipoRebateDto catTipoRebate = CatTipoRebateMapper.convertDto(entity.getCatTipoRebate());
			dto.setCatTipoRebate(catTipoRebate);
		}
		if (entity.getCatEstatusExclusion() != null) {
			CatEstatusExclusionDto estatus = CatEstatusExclusionMapper.convertDto(entity.getCatEstatusExclusion());
			dto.setCatEstatusExclusion(estatus);
		}
		if (entity.getCatTipoExclusion() != null) {
			CatTipoExclusionDto tipo = CatTipoExclusionMapper.convertDto(entity.getCatTipoExclusion());
			dto.setCatTipoExclusion(tipo);
		}
		return dto;
	}
	
	public static ExclusionEntity convertEntity(ExclusionDto dto) {
		ExclusionEntity entity = new ExclusionEntity();
		entity.setIdExclusion( dto.getIdExclusion() );
		entity.setFolio( dto.getFolio() ) ;
		entity.setComentario( dto.getComentario() );
		entity.setUsuarioSolicitud( UsuarioMapper.convertEntity( dto.getUsuarioSolicitud() ));
		entity.setUsuarioAutorizacion( UsuarioMapper.convertEntity( dto.getUsuarioAutorizacion() ));
		entity.setFechaHoraSolicitud( dto.getFechaHoraSolicitud() );
		entity.setFechaHoraAutorizacion( dto.getFechaHoraAutorizacion() );
		entity.setActivo( dto.isActivo() );
		entity.setContabilizado( dto.getContabilizado() );
		entity.setEvidencia( dto.getEvidencia() );
		entity.setImagen( dto.getImagen() );
		
		if (dto.getPeriodo() != null) {
			Periodo periodo = PeriodoMapper.convertEntity(dto.getPeriodo());
			entity.setPeriodo(periodo);
		}
		if (dto.getCatTipoRebate() != null) {
			TipoRebate tipoRebate = CatTipoRebateMapper.convertEntity(dto.getCatTipoRebate());
			entity.setCatTipoRebate(tipoRebate);
		}
		if (dto.getCatEstatusExclusion() != null) {
			CatEstatusExclusionEntity estatus = CatEstatusExclusionMapper.convertEntity(dto.getCatEstatusExclusion());
			entity.setCatEstatusExclusion(estatus);
		}
		if (dto.getCatTipoExclusion() != null) {
			CatTipoExclusionEntity tipo = CatTipoExclusionMapper.convertEntity(dto.getCatTipoExclusion());
			entity.setCatTipoExclusion(tipo);
		}
		if (dto.getUsuarioAutorizacion() != null) {
			Usuario usuario = UsuarioMapper.convertEntity(dto.getUsuarioAutorizacion());
			entity.setUsuarioAutorizacion(usuario);
		}
		if (dto.getUsuarioSolicitud() != null) {
			Usuario usuario = UsuarioMapper.convertEntity(dto.getUsuarioSolicitud());
			entity.setUsuarioSolicitud(usuario);
		}
		return entity;
	}

	public static List<ExclusionDto> convertDtos(List<ExclusionEntity> listEntities) {
		List<ExclusionDto> listDtos = new ArrayList<>();
		if (listEntities != null) {
			for (ExclusionEntity entity : listEntities) {
				listDtos.add( ExclusionMapper.convertDto(entity) );
			}
		}
		return listDtos;
	}

	public static ExclusionDto convertDtoImagen(ExclusionEntity entity) {
		ExclusionDto dto = new ExclusionDto();
		dto.setIdExclusion( entity.getIdExclusion() );
		dto.setEvidencia( entity.getEvidencia() );
		dto.setImagen( entity.getImagen() );
		return dto;
	}
}

package com.sodimac.rebates.service;

import java.util.List;

import com.sodimac.rebates.model.CatEventoDto;
import com.sodimac.rebates.model.entity.CatPermisoEntity;

public interface IEventoPermisoRolService {

	public List<CatEventoDto> getEventosPorIdPermiso(Integer pIdUsuario, Integer idPermiso);

	public List<CatEventoDto> getEventos(Integer pIdUsuario, List<CatPermisoEntity> listPermisos);
	
}

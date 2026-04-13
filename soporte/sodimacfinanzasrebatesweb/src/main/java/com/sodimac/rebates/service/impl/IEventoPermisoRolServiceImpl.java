package com.sodimac.rebates.service.impl;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.sodimac.rebates.mapper.CatEventoMapper;
import com.sodimac.rebates.model.CatEventoDto;
import com.sodimac.rebates.model.entity.CatPermisoEntity;
import com.sodimac.rebates.model.entity.EventoPermisoRolEntity;
import com.sodimac.rebates.repository.EventoPermisoRolRepository;
import com.sodimac.rebates.service.IEventoPermisoRolService;

@Service
public class IEventoPermisoRolServiceImpl implements IEventoPermisoRolService {

	@Autowired
	private EventoPermisoRolRepository eventoPermisoRolRepository;
	
	@Override
	public List<CatEventoDto> getEventosPorIdPermiso(Integer pIdUsuario, Integer idPermiso) {
		List<CatEventoDto> listEventos = new ArrayList<>();
		List<EventoPermisoRolEntity> list = this.eventoPermisoRolRepository.findEventosByIdPermiso(pIdUsuario, idPermiso);
		if (list != null) {
			for (EventoPermisoRolEntity entity : list) {
				listEventos.add( CatEventoMapper.convertToDto(entity.getCatEventoEntity()) );
			}
		}
		return listEventos;
	}

	
	@Override
	public List<CatEventoDto> getEventos(Integer pIdUsuario, List<CatPermisoEntity> listPermisos) {
		List<CatEventoDto> listEventosTotales = new ArrayList<>();
		if (listPermisos != null) {
			Map<Integer, Integer> mapEventos = new HashMap<>();
			
			for (CatPermisoEntity permiso : listPermisos) {
				List<CatEventoDto> listEventos = this.getEventosPorIdPermiso(pIdUsuario, permiso.getId());
				if (listEventos != null && listEventos.size() > 0) {
					
					for (CatEventoDto dto : listEventos) {
						if (! mapEventos.containsKey(dto.getIdCatEvento())) {
							listEventosTotales.add(dto);
							mapEventos.put(dto.getIdCatEvento(), dto.getIdCatEvento());
						}
					}
				}
			}
		}
		return listEventosTotales;
	}
	
}

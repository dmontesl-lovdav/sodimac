package com.sodimac.rebates.service.impl;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.sodimac.rebates.dto.CatFlujoEstatusDto;
import com.sodimac.rebates.dto.CatRolDto;
import com.sodimac.rebates.enums.EEstatus;
import com.sodimac.rebates.enums.EEvento;
import com.sodimac.rebates.mapper.CatFlujoEstatusMapper;
import com.sodimac.rebates.mapper.CatRolMapper;
import com.sodimac.rebates.model.entity.CatEventoEntity;
import com.sodimac.rebates.model.entity.CatFlujoEstatusEntity;
import com.sodimac.rebates.model.entity.CatRolEntity;
import com.sodimac.rebates.repository.CatFlujoEstatusRepository;
import com.sodimac.rebates.service.ICatFlujoEstatusService;

@Service
public class CatFlujoEstatusServiceImpl implements ICatFlujoEstatusService {

	@Autowired
	private CatFlujoEstatusRepository catFlujoEstatusRepository;
	
	@Override
	public List<CatFlujoEstatusDto> getCatFlujoEstatus(List<CatRolDto> rolesDto, EEvento eEvento, Integer estatusOrigen) {
		
		List<CatRolEntity> roles = CatRolMapper.convertToEntities(rolesDto);
		
		CatEventoEntity evento = new CatEventoEntity();
		evento.setIdCatEvento(eEvento.getId());
		
		List<CatFlujoEstatusEntity> entities = this.catFlujoEstatusRepository.findByRolInAndEventoAndEstatusOrigenAndActivo(roles, evento, estatusOrigen, EEstatus.ACTIVO.isActivo());
		if (entities != null) {
			return CatFlujoEstatusMapper.convertToDtos(entities);
		}
		return null;
	}

}

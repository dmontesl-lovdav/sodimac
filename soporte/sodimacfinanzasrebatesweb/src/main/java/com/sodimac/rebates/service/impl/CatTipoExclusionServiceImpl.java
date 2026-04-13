package com.sodimac.rebates.service.impl;

import java.util.List;

import javax.persistence.EntityManager;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.sodimac.rebates.dto.CatTipoExclusionDto;
import com.sodimac.rebates.enums.EEstatus;
import com.sodimac.rebates.mapper.CatTipoExclusionMapper;
import com.sodimac.rebates.model.entity.CatTipoExclusionEntity;
import com.sodimac.rebates.repository.CatTipoExclusionRepository;
import com.sodimac.rebates.service.ICatTipoExclusionService;

@Service
public class CatTipoExclusionServiceImpl implements ICatTipoExclusionService {
	
	@Autowired
    private EntityManager em;
	
	@Autowired
	private CatTipoExclusionRepository catTipoExclusionRepository;

	@Override
	public List<CatTipoExclusionDto> getCatTipoExclusion() {
		List<CatTipoExclusionDto> listDtos = null;
		List<CatTipoExclusionEntity> listEntities = this.catTipoExclusionRepository.findByActivo(EEstatus.ACTIVO.isActivo());
		if (listEntities != null) {
			listDtos = CatTipoExclusionMapper.convertDtos(listEntities);
		}
		return listDtos;
	}

	@SuppressWarnings("unchecked")
	@Override
	public List<CatTipoExclusionDto> getCatTipoExclusionPerfil(Integer idUser) {
		List<CatTipoExclusionDto> listDto = null;
		StringBuilder sbQry = new StringBuilder();
		sbQry.append("select IdCatTipoExclusion \r\n")
			 .append("     , ClaveExclusion \r\n")
			 .append("	   , TipoExclusion \r\n")
			 .append("from vw_perfilExclusion \r\n")
			 .append("where IdUsuario = ").append(idUser);
		
		List<Object[]> resultList = this.em.createNativeQuery(sbQry.toString()).getResultList();
		listDto = CatTipoExclusionMapper.convertObjectToDtos(resultList);
		return listDto;
	}
}

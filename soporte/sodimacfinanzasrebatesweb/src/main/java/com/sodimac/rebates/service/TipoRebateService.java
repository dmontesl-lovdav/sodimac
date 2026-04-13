package com.sodimac.rebates.service;

import java.util.List;
import java.util.Optional;

import javax.persistence.EntityManager;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.sodimac.rebates.dto.CatTipoRebateDto;
import com.sodimac.rebates.mapper.CatTipoRebateMapper;
import com.sodimac.rebates.model.TipoRebate;
import com.sodimac.rebates.repository.TipoRebateRepository;

@Service
public class TipoRebateService implements ITipoRebateService {
	
	@Autowired
    private EntityManager em;

	@Autowired
	private TipoRebateRepository tipoRebateRepo;

	@Override
	public List<TipoRebate> getAll() {

		return tipoRebateRepo.findAll();
	}

	@Override
	public List<CatTipoRebateDto> getActive() {
		List<TipoRebate> entities = tipoRebateRepo.findByActivo(true);
		return CatTipoRebateMapper.convertDtos(entities);
	}

	@Override
	public CatTipoRebateDto getById(Integer id) {
		Optional<TipoRebate> tipoRebate = tipoRebateRepo.findById(id);
		if(tipoRebate.isPresent()) {
			return CatTipoRebateMapper.convertDto(tipoRebate.get());
		}
		return null;
	}

	@SuppressWarnings("unchecked")
	@Override
	public List<CatTipoRebateDto> getTiposRebatesPerfil(Integer idUser) {
		List<CatTipoRebateDto> listDto = null;
		StringBuilder sbQry = new StringBuilder();
		sbQry.append("select IdCatTipoRebate \r\n")
			 .append("     , Nomenclatura \r\n")
			 .append("	   , TipoRebate \r\n")
			 .append("from vw_perfilRebate \r\n")
			 .append("where IdUsuario = ").append(idUser);
		
		List<Object[]> resultList = this.em.createNativeQuery(sbQry.toString()).getResultList();
		listDto = CatTipoRebateMapper.convertObjectToDtos(resultList);
		return listDto;
	}

}

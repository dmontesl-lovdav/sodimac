package com.sodimac.wsconfiguracion.service.config;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import javax.transaction.Transactional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;

import com.sodimac.wsconfiguracion.dto.CatRegimenFiscalDto;
import com.sodimac.wsconfiguracion.dto.ClientResponseTYPE;
import com.sodimac.wsconfiguracion.dto.RegimeFiscalDto;
import com.sodimac.wsconfiguracion.entity.config.CatRegimenFiscalEntity;
import com.sodimac.wsconfiguracion.entity.config.CatTipoPersonaEntity;
import com.sodimac.wsconfiguracion.repository.config.CatRegimenFiscalRepository;
import com.sodimac.wsconfiguracion.repository.config.CatTipoPersonaRepository;
import com.sodimac.wsconfiguracion.util.UtilsApi;
import com.sodimac.wsconfiguracion.util.enums.ECodigo;
import com.sodimac.wsconfiguracion.util.enums.ETipoPersonasSat;


@Service("catRegimenFiscalServiceImplConfig")
public class CatRegimenFiscalServiceImpl implements CatRegimenFiscalService {

	@Autowired
	@Qualifier("catRegimenFiscalRepositoryConfig")
	private CatRegimenFiscalRepository catRegimenFiscalRepository;
	
	@Autowired
	@Qualifier("catTipoPersonaRepositoryConfig")
	private CatTipoPersonaRepository catTipoPersonaRepository;

	@Override
	@Transactional
	public ClientResponseTYPE<List<CatRegimenFiscalDto>> obtieneRegimenFiscal(Integer idTipoPersona) {
		ClientResponseTYPE<List<CatRegimenFiscalDto>> regimenFiscal = new ClientResponseTYPE<List<CatRegimenFiscalDto>>(new ArrayList<CatRegimenFiscalDto>() );
		ETipoPersonasSat persona = Arrays.stream(ETipoPersonasSat.values()).filter(value -> value.getValor().equals(idTipoPersona)).findFirst().orElse(null);
		if (persona != null) {
			List<Integer> idsPersonas = new ArrayList<Integer>();
			if(persona.getValor() == ETipoPersonasSat.TODAS_LAS_PERSONAS.getValor()) {
				idsPersonas.add(ETipoPersonasSat.PERSONA_FISICA.getValor());
				idsPersonas.add(ETipoPersonasSat.PERSONA_MORAL.getValor());
			} else {
				idsPersonas.add(persona.getValor());
			}
			// TODO Auto-generated method stub
			List<CatTipoPersonaEntity> catTipoPersonaEntity = catTipoPersonaRepository.findByIdIn(idsPersonas);
			List<CatRegimenFiscalEntity>  catRegimenFiscalList = catRegimenFiscalRepository.findByCatTipoPersonaEntityInAndActivo(catTipoPersonaEntity, true);
			catRegimenFiscalList.stream().forEach((p)-> {
			
				regimenFiscal.getData().add(new CatRegimenFiscalDto (p.getRegimenfiscal()
						, p.getDescripcion()
						, p.getCatTipoPersonaEntity().getTipo()
						, p.getCatTipoPersonaEntity().getId()
					 ));
//				regimeFiscalDto.getRegimenFiscalDto().add(new CatRegimenFiscalDto (p.getRegimenfiscal()
//																					, p.getDescripcion()
//																					, p.getCatTipoPersonaEntity().getTipo()
//																					, p.getCatTipoPersonaEntity().getId()
//																				 ));
				});
			
		} else {
			UtilsApi.setRespuesta(regimenFiscal.getRespuesta(), ECodigo.ConfiguracionPersonasNoEncontrada);
		}
		
		return regimenFiscal;

	}

}

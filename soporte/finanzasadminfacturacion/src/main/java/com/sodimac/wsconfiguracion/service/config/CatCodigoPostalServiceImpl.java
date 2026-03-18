package com.sodimac.wsconfiguracion.service.config;

import java.util.Optional;

import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;

import com.sodimac.wsconfiguracion.dto.ClientResponseTYPE;
import com.sodimac.wsconfiguracion.dto.CodigoPostal;
import com.sodimac.wsconfiguracion.dto.CodigoPostalDto;
import com.sodimac.wsconfiguracion.entity.config.CatCodigoPostalEntity;
import com.sodimac.wsconfiguracion.repository.config.CatCodigoPostalRepository;
import com.sodimac.wsconfiguracion.util.UtilsApi;
import com.sodimac.wsconfiguracion.util.enums.ECodigo;


@Service("catCodigoPostalServiceImplConfig")
public class CatCodigoPostalServiceImpl implements CatCodigoPostalService {
	
    @Autowired
    private ModelMapper modelMapper;
	
	@Autowired
	@Qualifier("catCodigoPostalRepositoryConfig")
	private CatCodigoPostalRepository catCodigoPostalRepository;

	@Override
	public ClientResponseTYPE<CodigoPostal>  verificaCodigoPostal(Integer c_codigopostal) {
		// TODO Auto-generated method stub
		CodigoPostal codigoPostal = null;
		ClientResponseTYPE<CodigoPostal> codigoPostalResp = new ClientResponseTYPE<CodigoPostal>( new CodigoPostal());
		
		Optional<CatCodigoPostalEntity> catCodigoPostalEntity = catCodigoPostalRepository.findById(c_codigopostal);
		
		if (catCodigoPostalEntity.isPresent()) {
			codigoPostal =(CodigoPostal) convertirADto(catCodigoPostalEntity.get(), CodigoPostal.class);
			//codigoPostal.setCodigopostal(catCodigoPostalEntity.getId());
			codigoPostalResp.setData(codigoPostal);
		} else {
			codigoPostalResp.setData(null);
			UtilsApi.setRespuesta(codigoPostalResp.getRespuesta(), ECodigo.CodigoPostalNoExiste);
		}
		
		return codigoPostalResp;
	}
	
	
	private Object convertirADto(Object obj, Class<?> destinationClass) {
		Object dto = modelMapper.map(obj, destinationClass);
		return dto;
	}

}

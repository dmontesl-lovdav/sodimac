package com.sodimac.wsconfiguracion.service.config;

import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;

import com.sodimac.wsconfiguracion.dto.ClientResponseTYPE;
import com.sodimac.wsconfiguracion.dto.MesDto;
import com.sodimac.wsconfiguracion.dto.PeriodicidadDto;
import com.sodimac.wsconfiguracion.entity.config.CatMesesEntity;
import com.sodimac.wsconfiguracion.entity.config.CatPeriodicidadEntity;
import com.sodimac.wsconfiguracion.repository.config.CatMesesRepository;
import com.sodimac.wsconfiguracion.repository.config.CatPeriodicidadRepository;
import com.sodimac.wsconfiguracion.util.UtilsApi;
import com.sodimac.wsconfiguracion.util.enums.ECodigo;

@Service("catalogosServiceImplConfig")
public class CatalogosServiceImpl implements CatalogosService {

	@Autowired
	private ModelMapper modelMapper;
	
	@Autowired
	@Qualifier("catMesesRepositoryConfig")
	private CatMesesRepository catMesesRepository;
	
	@Autowired
	@Qualifier("catPeriodicidadRepositoryConfig")
	private CatPeriodicidadRepository catPeriodicidadRepository;
	
	
	@Override
	public ClientResponseTYPE<MesDto> getMesByClave(String clave) {
		MesDto mes = null;
		ClientResponseTYPE<MesDto> response = new ClientResponseTYPE<MesDto>( new MesDto());
		
		CatMesesEntity catMesesEntity = catMesesRepository.findByClave(clave);
		
		if (catMesesEntity != null) {
			mes = (MesDto) convertirADto(catMesesEntity, MesDto.class);
			//codigoPostal.setCodigopostal(catCodigoPostalEntity.getId());
			response.setData(mes);
		} else {
			response.setData(null);
			UtilsApi.setRespuesta(response.getRespuesta(), ECodigo.ConfigMesNoEncontrado);
		}
		
		return response;
	}

	@Override
	public ClientResponseTYPE<PeriodicidadDto> getPeriodicidadByClave(String clave) {
		PeriodicidadDto periodicidad = null;
		ClientResponseTYPE<PeriodicidadDto> response = new ClientResponseTYPE<PeriodicidadDto>( new PeriodicidadDto());
		
		CatPeriodicidadEntity catPeriodicidadEntity = catPeriodicidadRepository.findByClave(clave);
		
		if (catPeriodicidadEntity != null) {
			periodicidad = (PeriodicidadDto) convertirADto(catPeriodicidadEntity, PeriodicidadDto.class);
			//codigoPostal.setCodigopostal(catCodigoPostalEntity.getId());
			response.setData(periodicidad);
		} else {
			response.setData(null);
			UtilsApi.setRespuesta(response.getRespuesta(), ECodigo.PeriodicidadNoEncontrado);
		}
		
		return response;
	}

	private Object convertirADto(Object obj, Class<?> destinationClass) {
		Object dto = modelMapper.map(obj, destinationClass);
		return dto;
	}
}

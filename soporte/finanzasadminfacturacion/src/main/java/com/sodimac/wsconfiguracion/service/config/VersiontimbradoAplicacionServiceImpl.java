package com.sodimac.wsconfiguracion.service.config;

import javax.transaction.Transactional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;

import com.sodimac.wsconfiguracion.dto.ClientResponseTYPE;
import com.sodimac.wsconfiguracion.dto.VersionTimbradoDto;
import com.sodimac.wsconfiguracion.entity.config.VersiontimbradoAplicacionEntity;
import com.sodimac.wsconfiguracion.repository.config.VersiontimbradoAplicacionRepository;
import com.sodimac.wsconfiguracion.util.UtilsApi;
import com.sodimac.wsconfiguracion.util.enums.ECodigo;

@Service
public class VersiontimbradoAplicacionServiceImpl implements VersiontimbradoAplicacionService {
	
   
	@Autowired
	@Qualifier("versiontimbradoAplicacionRepositoryConfig")
	private VersiontimbradoAplicacionRepository versiontimbradoAplicacionRepository;

	@Override
	@Transactional
	public ClientResponseTYPE<VersionTimbradoDto> ObtieneVersionTimbrado(Integer idAplicacion) {
		ClientResponseTYPE<VersionTimbradoDto> versionTimbrado = new ClientResponseTYPE<VersionTimbradoDto>(new VersionTimbradoDto());
		VersiontimbradoAplicacionEntity entity = versiontimbradoAplicacionRepository.findByIdcataplicacionesAndActivo(idAplicacion, true);
		if(entity != null) {
			String version = entity.getCatVersionTimbrado().getVersion();
			versionTimbrado.getData().setVersion(version);
		} else {
			UtilsApi.setRespuesta(versionTimbrado.getRespuesta(), ECodigo.ConfiguracionVersionNoEncontrado);
		}
		
		return versionTimbrado;
	}

}

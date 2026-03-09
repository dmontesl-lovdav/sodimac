package com.sodimac.cfdi.service.documento.impl;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.sodimac.cfdi.entity.fiscal.ConfiguracionFtpEntity;
import com.sodimac.cfdi.models.documento.ConfiguracionFtpModel;
import com.sodimac.cfdi.repository.fiscal.documento.ConfiguracionFtpRepository;
import com.sodimac.cfdi.service.documento.ConfiguracionFtpService;

@Service
public class ConfiguracionFtpServiceImpl implements ConfiguracionFtpService {

	@Autowired
	private ConfiguracionFtpRepository configuracionFtpRepository;
	
	@Override
	public ConfiguracionFtpModel getConfiguracion(Integer idConfiguracionFtp) {
		ConfiguracionFtpModel model = new ConfiguracionFtpModel();
		ConfiguracionFtpEntity confEntity = this.configuracionFtpRepository.findByIdConfiguracionFtp(idConfiguracionFtp);
		if (confEntity != null) {
			model.setIdConfiguracionFtp( confEntity.getIdConfiguracionFtp() );
			model.setUsuario( confEntity.getUsuario() );
			model.setDescripcion( confEntity.getDescripcion() );
			model.setContrasenia( confEntity.getContrasenia() );
			model.setPuerto( confEntity.getPuerto() );
			model.setUrl( confEntity.getUrl() );
			model.setEstatus( confEntity.getEstatus() );
			model.setFechaCreacion( confEntity.getFechaCreacion() );
		}
		return model;
	}

}

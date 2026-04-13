package com.sodimac.rebates.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.sodimac.rebates.model.Configuracion;
import com.sodimac.rebates.repository.ConfiguracionRepository;

@Service
public class ConfiguracionService implements IConfiguracionService {

	@Autowired
	private ConfiguracionRepository configuracionRepository;
	
	@Override
	public String getValor(String nombreVariable) {
		Configuracion configuracion = this.configuracionRepository.findByNombreVariable(nombreVariable);
		if (configuracion != null) {
			return configuracion.getValor();
		}
		return null;
	}

}

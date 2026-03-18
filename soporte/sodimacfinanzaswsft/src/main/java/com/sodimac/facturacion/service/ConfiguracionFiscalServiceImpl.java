package com.sodimac.facturacion.service;

import java.util.HashMap;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.sodimac.facturacion.entity.fis.CatConfiguracionEntity;
import com.sodimac.facturacion.repository.fis.CatConfiguracionRepository;

@Service
public class ConfiguracionFiscalServiceImpl implements ConfiguracionFiscalService {

	@Autowired
    private CatConfiguracionRepository catConfiguracionRepository;

	HashMap<String, String> parametros;

	public HashMap<String, String> getConfig() {
		if (parametros==null) {
			parametros = new HashMap<>();
			
	    	List<CatConfiguracionEntity> list = catConfiguracionRepository.findAll();
	    	list.forEach(item -> {
	    		parametros.put(item.getNombreCampo(),item.getValor());
	    	});			
		}
		
		return parametros;
	}

}

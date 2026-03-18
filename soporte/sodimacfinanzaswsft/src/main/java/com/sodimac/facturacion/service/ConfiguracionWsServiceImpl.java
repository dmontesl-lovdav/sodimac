package com.sodimac.facturacion.service;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.EnableTransactionManagement;
import org.springframework.transaction.annotation.Transactional;

import com.sodimac.facturacion.entity.ws.ConfiguracionWsEntity;
import com.sodimac.facturacion.repository.ws.ConfiguracionWsRepository;

@Service
@EnableTransactionManagement
public class ConfiguracionWsServiceImpl implements ConfiguracionWsService {

	@Autowired
	private ConfiguracionWsRepository configuracionWsRepository;
	
	@Override
	@Transactional(transactionManager="transactionManagerWs", readOnly=true)
	public List<ConfiguracionWsEntity> getAll() {
		return configuracionWsRepository.findAll();
	}
	
	@Override
	@Transactional(transactionManager="transactionManagerWs", readOnly=true)
	public String findParameterByKey(String NombreCampo) {
		return configuracionWsRepository.findParameterByKey(NombreCampo);
	}
	
}

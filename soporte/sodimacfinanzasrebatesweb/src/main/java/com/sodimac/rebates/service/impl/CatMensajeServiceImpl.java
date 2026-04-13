package com.sodimac.rebates.service.impl;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.sodimac.rebates.enums.EEstatus;
import com.sodimac.rebates.enums.EMensaje;
import com.sodimac.rebates.model.entity.CatMensajeEntity;
import com.sodimac.rebates.repository.CatMensajeRepository;
import com.sodimac.rebates.service.ICatMensajeService;

@Service
public class CatMensajeServiceImpl implements ICatMensajeService {

	@Autowired
	private CatMensajeRepository catMensajeRepository; 
	
	@Override
	public String getMensaje(EMensaje clave) {
		CatMensajeEntity entity = this.catMensajeRepository.findByClaveAndActivo(clave.toString(), EEstatus.ACTIVO.isActivo());
		return entity.getDescripcion();
	}

}

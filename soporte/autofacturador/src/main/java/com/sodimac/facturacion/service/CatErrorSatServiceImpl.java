package com.sodimac.facturacion.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.sodimac.facturacion.repository.CatErrorSatRepository;

@Service
public class CatErrorSatServiceImpl implements CatErrorSatService {

	@Autowired
	private CatErrorSatRepository catErrorSatRepository;
	
	@Override
	public String obtenerMensajeErrorSat(String pTicket) {
		return catErrorSatRepository.obtenerMensajeErrorSat(pTicket);
	}

}

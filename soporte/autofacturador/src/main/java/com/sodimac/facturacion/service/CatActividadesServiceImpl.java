package com.sodimac.facturacion.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.sodimac.facturacion.entity.CatActividadesEntity;
import com.sodimac.facturacion.repository.CatActividadesRepository;

@Service
public class CatActividadesServiceImpl implements CatActividadesService {

	@Autowired
	private CatActividadesRepository catActividadesRepository;
	@Autowired
	private SeguridadService seguridadService;
	
	@Transactional
	public int registrarActividad(
			int idActividad, 
			  String actividadDesc
			, String ticket
			, int usuario
			, String longitud
			, String latitud
			, String pagina
			, String explorador
			, String sistemaOper
			, String ip
			, String rfc
			, String sessionId
			) {
		
		
		return catActividadesRepository.registrarActividad(idActividad
				, actividadDesc
				, ticket
				, usuario
				, longitud
				, latitud
				, pagina
				, explorador
				, sistemaOper
				, ip
				, seguridadService.encriptar(rfc)
				, sessionId
				);
	}

	
	@Override
	public CatActividadesEntity getActividad(int idActividad) {
		
		return catActividadesRepository.findById(idActividad).orElse(null);
	}
	
	




	
}


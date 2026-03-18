package com.sodimac.wsconfiguracion.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.sodimac.wsconfiguracion.entity.config.BitActividadesEntity;
import com.sodimac.wsconfiguracion.repository.config.BitActividadesRepository;

@Service
public class CatActividadesServiceImpl implements CatActividadesService {

	@Autowired
	@Qualifier("bitActividadesRepositoryConfig")
	private BitActividadesRepository bitActividadesRepository;
//	@Autowired
//	private SeguridadService seguridadService;
	
	@Override
	//@Transactional("transactionManagerConfig")
	@Transactional()
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
		
		
		BitActividadesEntity bitActividadesEntity = new BitActividadesEntity();
		bitActividadesEntity.setIdActividad(idActividad);
		bitActividadesEntity.setDescripcion(actividadDesc);
		bitActividadesEntity.setTicket(ticket);
		bitActividadesEntity.setSistemaOperativo(sistemaOper);
		bitActividadesEntity.setDireccionIp(ip);
		bitActividadesEntity.setExplorador(explorador);


		bitActividadesEntity = bitActividadesRepository.save(bitActividadesEntity);
		return 0;
	}


	

	
	




	
}


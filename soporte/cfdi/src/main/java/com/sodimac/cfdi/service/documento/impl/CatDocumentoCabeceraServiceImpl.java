package com.sodimac.cfdi.service.documento.impl;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.sodimac.cfdi.entity.fiscal.CatDocumentoCabeceraEntity;
import com.sodimac.cfdi.models.documento.CatDocumentoCabeceraModel;
import com.sodimac.cfdi.repository.fiscal.documento.CatDocumentoCabeceraRepository;
import com.sodimac.cfdi.service.documento.CatDocumentoCabeceraService;

@Service
public class CatDocumentoCabeceraServiceImpl implements CatDocumentoCabeceraService {

	@Autowired
	private CatDocumentoCabeceraRepository cabeceraRepository;
	
	@Override
	public CatDocumentoCabeceraModel getCatDocumentoCabecera(Integer idDocumentoCabecera) {
		CatDocumentoCabeceraModel model = new CatDocumentoCabeceraModel();
		CatDocumentoCabeceraEntity catDocEntity = this.cabeceraRepository.findByIdDocumentoCabecera(idDocumentoCabecera);
		if (catDocEntity != null) {
			model.setIdDocumentoCabecera( catDocEntity.getIdDocumentoCabecera() );
			model.setIdTipoDocumento( catDocEntity.getIdTipoDocumento() );
			model.setNumeroColumnas( catDocEntity.getNumeroColumnas() );
			model.setFilaComienza( catDocEntity.getFilaComienza() );
			model.setCaracterSeparador( catDocEntity.getCaracterSeparador() );
			model.setEstatus( catDocEntity.getEstatus() );
			model.setFechaCreacion( catDocEntity.getFechaCreacion() );
		}
		return model;
	}
}

package com.sodimac.cfdi.service.documento.impl;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.sodimac.cfdi.entity.fiscal.CatDocumentoConfEntity;
import com.sodimac.cfdi.models.documento.CatDocumentoConfModel;
import com.sodimac.cfdi.repository.fiscal.documento.CatDocumentoConfRepository;
import com.sodimac.cfdi.service.documento.CatDocumentoConfService;

@Service
public class CatDocumentoConfServiceImpl implements CatDocumentoConfService {
	
	@Autowired
	private CatDocumentoConfRepository catDocumentoConfRepository;

	@Override
	public CatDocumentoConfModel getCatDocumentoConf(Integer idDocumentoConf) {
		CatDocumentoConfModel model = new CatDocumentoConfModel();
		CatDocumentoConfEntity catDocConfEntity = this.catDocumentoConfRepository.findByIdDocumentoConf(idDocumentoConf);
		if (catDocConfEntity != null) {
			model.setIdDocumentoConf( catDocConfEntity.getIdDocumentoConf() );
			model.setIdDocumentoCabecera( catDocConfEntity.getIdDocumentoCabecera() );
			model.setIdConfiguracionFtp( catDocConfEntity.getIdConfiguracionFtp() );
			model.setNombreConfiguracion( catDocConfEntity.getNombreConfiguracion() );
			model.setExtension( catDocConfEntity.getExtension() );
			model.setRutaDeposito( catDocConfEntity.getRutaDeposito() );
			model.setEstatus( catDocConfEntity.getEstatus() );
			model.setFechaCreacion( catDocConfEntity.getFechaCreacion() );
		}
		return model;
	}

}

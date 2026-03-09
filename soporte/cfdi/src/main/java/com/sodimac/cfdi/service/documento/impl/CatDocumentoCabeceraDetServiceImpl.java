package com.sodimac.cfdi.service.documento.impl;

import java.util.ArrayList;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.sodimac.cfdi.entity.fiscal.CatDocumentoCabeceraDetEntity;
import com.sodimac.cfdi.models.documento.CatDocumentoCabeceraDetModel;
import com.sodimac.cfdi.repository.fiscal.documento.CatDocumentoCabeceraDetRepository;
import com.sodimac.cfdi.service.documento.CatDocumentoCabeceraDetService;

@Service
public class CatDocumentoCabeceraDetServiceImpl implements CatDocumentoCabeceraDetService {

	@Autowired
	private CatDocumentoCabeceraDetRepository cabeceraDetRepository;
	
	@Override
	public List<CatDocumentoCabeceraDetModel> getCabecera(Integer idDocumentoCabecera, Integer estatus) {
		List<CatDocumentoCabeceraDetModel> listCabecera = new ArrayList<CatDocumentoCabeceraDetModel>();
		List<CatDocumentoCabeceraDetEntity> listCabecerasEntity = this.cabeceraDetRepository.findByIdDocumentoCabeceraAndEstatus(idDocumentoCabecera, estatus);
		if (listCabecerasEntity != null) {
			for (CatDocumentoCabeceraDetEntity entity : listCabecerasEntity) {
				CatDocumentoCabeceraDetModel model = new CatDocumentoCabeceraDetModel();
				model.setIdDocumentoCabeceraDet( entity.getIdDocumentoCabeceraDet() );
				model.setIdDocumentoCabecera( entity.getIdDocumentoCabecera() );
				model.setNombre( entity.getNombre() );
				model.setPosicion( entity.getPosicion() );
				
				listCabecera.add(model);
			}
		}
		return listCabecera;
	}

}

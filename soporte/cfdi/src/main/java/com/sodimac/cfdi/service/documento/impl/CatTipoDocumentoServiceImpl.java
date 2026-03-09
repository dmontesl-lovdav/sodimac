package com.sodimac.cfdi.service.documento.impl;

import java.util.ArrayList;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Isolation;
import org.springframework.transaction.annotation.Transactional;

import com.google.gson.Gson;
import com.sodimac.cfdi.entity.fiscal.CatTipoDocumentoEntity;
import com.sodimac.cfdi.models.SelectItem;
import com.sodimac.cfdi.models.documento.CatTipoDocumentoModel;
import com.sodimac.cfdi.repository.fiscal.documento.CatTipoDocumentoRepository;
import com.sodimac.cfdi.service.documento.CatTipoDocumentoService;

@Service
public class CatTipoDocumentoServiceImpl implements CatTipoDocumentoService {

	@Autowired
	private CatTipoDocumentoRepository catTipoDocumentoRepository;
	
	@Override
	public List<CatTipoDocumentoModel> getTiposDocumento() {
		List<CatTipoDocumentoEntity> listTiposDocEnt = this.catTipoDocumentoRepository.findTiposDocumento();
		List<CatTipoDocumentoModel> lisTipoDoc = this.convert(listTiposDocEnt);
		return lisTipoDoc;
	}
	
	@Transactional(isolation = Isolation.READ_UNCOMMITTED)
	@Override
	public String getTiposDocumentoGson() {
		
		List<SelectItem> list = new ArrayList<SelectItem>();
		
		this.catTipoDocumentoRepository.findTiposDocumento().forEach(item -> {
			SelectItem itemList = new SelectItem();
			itemList.setId(item.getIdTipoDocumento().toString());
			itemList.setDescripcion(item.getNombre());
			list.add(itemList);			
		});
		Gson gson= new Gson();
		String resultado = gson.toJson(list);
		return resultado;
	}
	
	private List<CatTipoDocumentoModel> convert(List<CatTipoDocumentoEntity> listTiposDocEnt) {
		List<CatTipoDocumentoModel> lisTipoDoc = new ArrayList<>();
		if (listTiposDocEnt != null) {
			for (CatTipoDocumentoEntity tipoDoc : listTiposDocEnt) {
				CatTipoDocumentoModel model = new CatTipoDocumentoModel();
				model.setIdTipoDocumento( tipoDoc.getIdTipoDocumento() );
				model.setNombre( tipoDoc.getNombre() );
				model.setDescripcion( tipoDoc.getDescripcion() );
				model.setEstatus( tipoDoc.getEstatus() );
				lisTipoDoc.add(model);
			}
		}
		return lisTipoDoc;
	}

}

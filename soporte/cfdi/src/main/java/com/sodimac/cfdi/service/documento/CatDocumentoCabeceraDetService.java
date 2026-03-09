package com.sodimac.cfdi.service.documento;

import java.util.List;

import com.sodimac.cfdi.models.documento.CatDocumentoCabeceraDetModel;

public interface CatDocumentoCabeceraDetService {

	public List<CatDocumentoCabeceraDetModel> getCabecera(Integer idDocumentoCabecera, Integer estatus);
}

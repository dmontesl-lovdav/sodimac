package com.sodimac.cfdi.service.documento;

import java.util.List;

import com.sodimac.cfdi.models.documento.CatTipoDocumentoModel;

public interface CatTipoDocumentoService {

	public List<CatTipoDocumentoModel> getTiposDocumento();

	public String getTiposDocumentoGson(); 
	
}

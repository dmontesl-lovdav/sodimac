package com.sodimac.cfdi.service.documento;

import java.util.List;

import org.springframework.web.multipart.MultipartFile;

import com.sodimac.cfdi.models.documento.DocumentoPublicadoModel;
import com.sodimac.cfdi.models.documento.Generic;

public interface DocumentoPublicadoService {

	public List<DocumentoPublicadoModel> getDocumentosPublicados(String pfechaInicial, String pfechafinal, Integer pIdTipoDocumento);

	public Generic createDocument(MultipartFile multiPart, Integer idTipoDocumento, Integer idUser);

	public String borrarArchivo(Integer idDocumentoPublicado);
	
}

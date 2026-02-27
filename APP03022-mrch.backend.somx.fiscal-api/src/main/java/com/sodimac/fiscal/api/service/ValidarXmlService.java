package com.sodimac.fiscal.api.service;

import org.springframework.http.ResponseEntity;
import org.springframework.web.multipart.MultipartFile;

public interface ValidarXmlService {

	public ResponseEntity<Object> validaXmlFiscal(MultipartFile file, int pacPrioidad);
	
}

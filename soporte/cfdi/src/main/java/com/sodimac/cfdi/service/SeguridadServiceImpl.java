package com.sodimac.cfdi.service;

import org.springframework.stereotype.Service;

import com.sodimac.cfdi.util.UtilsApi;
import com.sodimac.lib.crypto.service.CryptoService;
import com.sodimac.lib.crypto.service.CryptoServiceImpl;

@Service
public class SeguridadServiceImpl implements SeguridadService {
	
	CryptoService crypto;
		
	public String encriptar(String textToEncrypt) {
		
		inicializar();
		if (textToEncrypt.isEmpty()) return "";
		return crypto.encrypt(textToEncrypt);		
	}
		
	public String desencriptar(String encryptedText) {
		
		inicializar();		
		if (encryptedText.isEmpty()) return "";
		return crypto.decrypt(encryptedText);
	}
	
	public void inicializar() {
		
		if (crypto == null) {
			String path = UtilsApi.getPathCifradoProperties();
			crypto = new CryptoServiceImpl(path);
		}
	}
		
}

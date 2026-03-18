package com.sodimac.facturacion.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.sodimac.lib.crypto.service.CryptoService;
import com.sodimac.lib.crypto.service.CryptoServiceImpl;

@Service
public class SeguridadServiceImpl implements SeguridadService {
	
	@Autowired
	private CatConfiguracionService catConfiguracionService;

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
			String path = catConfiguracionService.findParameterByKey("Encrypt.Path");
			crypto = new CryptoServiceImpl(path);
		}
	}
		
}

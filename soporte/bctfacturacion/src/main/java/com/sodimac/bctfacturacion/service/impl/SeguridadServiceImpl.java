package com.sodimac.bctfacturacion.service.impl;

import org.springframework.stereotype.Service;

import com.sodimac.bctfacturacion.service.SeguridadService;
import com.sodimac.lib.crypto.service.CryptoService;
import com.sodimac.lib.crypto.service.CryptoServiceImpl;

@Service
public class SeguridadServiceImpl implements SeguridadService {

	private CryptoService crypto;
	
	@Override
	public String encriptar(String textToEncrypt) {
		this.inicializar();
		if (textToEncrypt.isEmpty()) {
			return "";
		}
		return crypto.encrypt(textToEncrypt);
	}

	@Override
	public String desencriptar(String encryptedText) {
		this.inicializar();
		if (encryptedText.isEmpty()) {
			return "";
		}
		return crypto.decrypt(encryptedText);
	}

	public void inicializar() {
		if (crypto == null) {
			String cryptoFile =  System.getenv("SODIMAC_CRYPTO_FILE");
			crypto = new CryptoServiceImpl(cryptoFile);
		}
	}
}

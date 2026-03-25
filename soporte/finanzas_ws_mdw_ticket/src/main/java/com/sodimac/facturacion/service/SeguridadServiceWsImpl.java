package com.sodimac.facturacion.service;

import java.io.UnsupportedEncodingException;

import org.bouncycastle.crypto.DataLengthException;
import org.bouncycastle.crypto.InvalidCipherTextException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import com.sodimac.facturacion.util.UtilsApi;
import com.sodimac.lib.cifrado.service.CryptoService;
import com.sodimac.lib.cifrado.service.CryptoServiceImpl;

@Service
public class SeguridadServiceWsImpl implements SeguridadServiceWs {
	
	CryptoService crypto;
	Logger logger = LoggerFactory.getLogger(SeguridadServiceWsImpl.class);
		
	public String encriptar(String textToEncrypt) throws DataLengthException, IllegalStateException, InvalidCipherTextException, UnsupportedEncodingException {
		
		if (textToEncrypt.isEmpty()) return "";
		inicializar();
		return crypto.encrypt(textToEncrypt);		
	}
		
	public String desencriptar(String encryptedText) throws DataLengthException, IllegalStateException, InvalidCipherTextException, UnsupportedEncodingException {
		
		if (encryptedText.isEmpty()) return "";
		inicializar();
		return crypto.decrypt(encryptedText);
	}
	
	public void inicializar() throws UnsupportedEncodingException {
		
		if (crypto == null) {
			crypto = new CryptoServiceImpl(UtilsApi.getPathCifradoProperties());
		}
	}
		
}

package com.sodimac.rebates.service;

import java.io.UnsupportedEncodingException;

import org.bouncycastle.crypto.DataLengthException;
import org.bouncycastle.crypto.InvalidCipherTextException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import com.sodimac.lib.cifrado.service.CryptoService;
import com.sodimac.lib.cifrado.service.CryptoServiceImpl;
import com.sodimac.rebates.util.Util;

@Service
public class SeguridadService implements ISeguridadService {

	CryptoService crypto;
	Logger logger = LoggerFactory.getLogger(SeguridadService.class);

	public String encriptar(String textToEncrypt) throws DataLengthException, IllegalStateException,
			InvalidCipherTextException, UnsupportedEncodingException {

		if (textToEncrypt.isEmpty()) {

			return "";
		}

		inicializar();
		return crypto.encrypt(textToEncrypt);
	}

	public String desencriptar(String encryptedText) throws DataLengthException, IllegalStateException,
			InvalidCipherTextException, UnsupportedEncodingException {

		if (encryptedText.isEmpty()) {

			return "";
		}
		inicializar();
		return crypto.decrypt(encryptedText);
	}

	public void inicializar() throws UnsupportedEncodingException {

		if (crypto == null) {

			crypto = new CryptoServiceImpl(Util.getPathCifradoProperties());
		}
	}

}

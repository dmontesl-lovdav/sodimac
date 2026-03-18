package com.sodimac.facturacion.service;

import java.io.UnsupportedEncodingException;

import org.bouncycastle.crypto.DataLengthException;
import org.bouncycastle.crypto.InvalidCipherTextException;

public interface SeguridadServiceWs {
	
	public String encriptar(String textToEncrypt) throws DataLengthException, IllegalStateException, InvalidCipherTextException, UnsupportedEncodingException;
	public String desencriptar(String encryptedText) throws DataLengthException, IllegalStateException, InvalidCipherTextException, UnsupportedEncodingException;
}

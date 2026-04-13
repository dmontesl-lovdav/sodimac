package com.sodimac.rebates.service;

import java.io.UnsupportedEncodingException;

import org.bouncycastle.crypto.DataLengthException;
import org.bouncycastle.crypto.InvalidCipherTextException;

public interface ISeguridadService {

	public String encriptar(String textToEncrypt)
			throws DataLengthException, IllegalStateException, InvalidCipherTextException, UnsupportedEncodingException;

	public String desencriptar(String encryptedText)
			throws DataLengthException, IllegalStateException, InvalidCipherTextException, UnsupportedEncodingException;
}

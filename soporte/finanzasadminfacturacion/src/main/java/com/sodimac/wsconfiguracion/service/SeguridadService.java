package com.sodimac.wsconfiguracion.service;

public interface SeguridadService {
	
	public String encriptar(String textToEncrypt) throws Exception;
	public String desencriptar(String encryptedText) throws Exception;
}

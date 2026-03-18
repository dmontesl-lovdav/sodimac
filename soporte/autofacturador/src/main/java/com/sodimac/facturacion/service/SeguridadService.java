package com.sodimac.facturacion.service;

public interface SeguridadService {
	
	public String encriptar(String textToEncrypt);
	public String desencriptar(String encryptedText);
}

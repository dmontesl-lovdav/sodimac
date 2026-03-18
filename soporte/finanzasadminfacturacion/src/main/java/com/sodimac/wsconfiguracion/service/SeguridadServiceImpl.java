package com.sodimac.wsconfiguracion.service;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;

import com.sodimac.lib.crypto.service.CryptoService;
import com.sodimac.lib.crypto.service.CryptoServiceImpl;
import com.sodimac.wsconfiguracion.service.config.CatConfiguracionService;

@Service
public class SeguridadServiceImpl implements SeguridadService {
	
	@Autowired
	@Qualifier("catConfiguracionServiceImplConfig")
	private CatConfiguracionService catConfiguracionService;


	CryptoService crypto;
		
	public String encriptar(String textToEncrypt) throws Exception {
		
		inicializar();
		if (textToEncrypt.isEmpty()) return "";
		return crypto.encrypt(textToEncrypt);		
	}
		
	public String desencriptar(String encryptedText) throws Exception {
		
		inicializar();		
		if (encryptedText.isEmpty()) return "";
		return crypto.decrypt(encryptedText);
	}
	
	public void inicializar() throws Exception {
		
		if (crypto == null) {
			String path = catConfiguracionService.findParameterByKey("ENCRYPT_PATH");
			path = "C:\\workspace_sts\\finanzasadminfacturacion\\src\\main\\resources\\crypto.properties";
			
			Path path2 = Paths.get(path);
			Boolean existPath = Files.exists(path2);
			if (!existPath) {
				throw new Exception("No existe el path: " + path);
			}
			crypto = new CryptoServiceImpl(path);
			
//			System.out.println("rfc" + crypto.encrypt("XIA190128J61"));
//			System.out.println("razonSocial" + crypto.encrypt("XENON INDUSTRIAL ARTICLES S DE CV"));
		}
	}
		
}

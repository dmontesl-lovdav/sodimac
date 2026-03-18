package com.sodimac.facturacion;

import java.io.UnsupportedEncodingException;

import org.bouncycastle.crypto.DataLengthException;
import org.bouncycastle.crypto.InvalidCipherTextException;
import org.junit.jupiter.api.Test;

import com.sodimac.facturacion.util.UtilsApi;
import com.sodimac.lib.cifrado.service.CryptoService;
import com.sodimac.lib.cifrado.service.CryptoServiceImpl;

public class WSCriptoTest {

	private CryptoService crypto;
	
	//@Test
	public void test() {
		
		String url = "jdbc:mysql://10.138.150.74:3306/wsfacturacion?useUnicode=true&useJDBCCompliantTimezoneShift=true&useLegacyDatetimeCode=false&serverTimezone=CST6CDT";
		try {
			String urlEncriptada = this.encriptar(url);
			System.out.println( urlEncriptada );
		} catch (DataLengthException | IllegalStateException | InvalidCipherTextException
				| UnsupportedEncodingException e) {
			e.printStackTrace();
		}
	}
	
	@Test
	public void desencriptarTest() {
		
		String urlEncriptada = "MDAwMDAwMDAwMDAwMDAwMG9P6VRjJbgY4HNO+fz5AOvRRKRf5VhSSm6zjvvxs8cRnKmjjtkTCMBoRNM7XWU42t76KKYfPmI9IBVAaf7TGMGOB6org1Ub0fwrWirA2ZpPRu6QJ9RQsKvXEyooYYrKj6p7tLJ/jb+7hWe+t6iEM3/wLiueyMDj4Us1mdOyiyjTNmM0OtoYsCmk3ht1mT4erMdmGvSDbPIvSS9MZ8o53oU=";
		String userEncriptada = "MDAwMDAwMDAwMDAwMDAwMJNlPAdn5r2/nDru1ARLfgKMYWOoXXZIc7xzk0jaB+s8";
		String passEncriptada = "MDAwMDAwMDAwMDAwMDAwMJNlPAdn5r2/nDru1ARLfgKMYWOoXXZIc7xzk0jaB+s8";
		
		try {
			System.out.println("jdbc.ws.url=" + this.desencriptar(urlEncriptada));
			System.out.println("jdbc.ws.user=" + this.desencriptar(userEncriptada));
			System.out.println("jdbc.ws.pass=" + this.desencriptar(passEncriptada));
		} catch (DataLengthException | IllegalStateException | InvalidCipherTextException
				| UnsupportedEncodingException e) {
			e.printStackTrace();
		}
	}
		
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
			String pathCifradoProperties = UtilsApi.getPathCifradoProperties();
			crypto = new CryptoServiceImpl( pathCifradoProperties );
		}
	}
}

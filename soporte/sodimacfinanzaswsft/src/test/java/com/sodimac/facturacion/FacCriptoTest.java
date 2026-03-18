package com.sodimac.facturacion;

import java.io.UnsupportedEncodingException;

import org.bouncycastle.crypto.DataLengthException;
import org.bouncycastle.crypto.InvalidCipherTextException;
import org.junit.jupiter.api.Test;

import com.sodimac.facturacion.util.UtilsApi;
import com.sodimac.lib.cifrado.service.CryptoService;
import com.sodimac.lib.cifrado.service.CryptoServiceImpl;

public class FacCriptoTest {

	private CryptoService crypto;
	
	@Test
	public void test() {
		
		String url = "jdbc:mysql://10.138.150.74:3306/facturacion?useUnicode=true&useJDBCCompliantTimezoneShift=true&useLegacyDatetimeCode=false&serverTimezone=CST6CDT";
		String user = "facturaUser";
		String pass = "facturaUser";
		try {
			
			String urlEncriptada = this.encriptar(url);
			String userEncriptada = this.encriptar(user);
			String passEncriptada = this.encriptar(pass);
			
			System.out.println("jdbc.ws.url=" + urlEncriptada);
			System.out.println("jdbc.ws.user=" + userEncriptada);
			System.out.println("jdbc.ws.pass=" + passEncriptada);
			
			
			System.out.println("jdbc.ws.url=" + this.desencriptar(urlEncriptada));
			System.out.println("jdbc.ws.user=" + this.desencriptar(userEncriptada));
			System.out.println("jdbc.ws.pass=" + this.desencriptar(passEncriptada));
			
		} catch (DataLengthException | IllegalStateException | InvalidCipherTextException
				| UnsupportedEncodingException e) {
			e.printStackTrace();
		}
	}
	
	@Test
	public void desencriptarTest() {
		
		System.out.println("\n\n");
		
		String urlEncriptada = "MDAwMDAwMDAwMDAwMDAwMLR3eMB6Bxutd8IuDqt8tqBf70URBDtEL/mWAxP5vna5eEhgZMA4EItOjIvt60uHU9VvBCOjbpHf7YMmKzg4MrVazYDnu64LQ+eMGBJA+NCtR6Wfki9brawxwZgPITCLDELjH0cYFZkOqug8VZCAjcDQLrDWy0dkDRav9nK7SYfgeG+C6FJKYwFz15t4qfxV+A==";
		String userEncriptada = "MDAwMDAwMDAwMDAwMDAwMKG+OSYC8t9vgQT42Et+pfY=";
		String passEncriptada = "MDAwMDAwMDAwMDAwMDAwMKG+OSYC8t9vgQT42Et+pfY=";
		
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

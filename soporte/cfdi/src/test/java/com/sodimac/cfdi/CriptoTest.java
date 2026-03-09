package com.sodimac.cfdi;

import org.junit.jupiter.api.Test;

import com.sodimac.lib.crypto.service.CryptoService;
import com.sodimac.lib.crypto.service.CryptoServiceImpl;


public class CriptoTest {

	private CryptoService crypto;
	
	@Test
	public void testAdm() {
		String urlEncriptado = "97fa82ef7a6718fac327ae724fa824e6a7b29b67bb2c9e8d54ce97f29528aa105d006deb8b68f1c45a3762970dfd3120f254bc151a5c3a7ec1c02a86c15bfed7a697c9e9f121c65a4b2cd38c7507b6505af42292379192671db53e3ba9679628145db4c4e6a3e39bead61d7236dd9e91ef0b93a4fcb1c19dde662a0f7c54d09039b3d06cff4f86933b86c7f45603e5f5";
		String urlUser = "9f757d0216db82fdb1714242a21a1aab";
		String urlPassword = "9f757d0216db82fdb1714242a21a1aab";
		
		System.out.println("jdbc.adm.url="+this.desencriptar(urlEncriptado));
		System.out.println("jdbc.adm.user="+this.desencriptar(urlUser));
		System.out.println("jdbc.adm.pass="+this.desencriptar(urlPassword));
	}
	
	//@Test
	public void testBct() {
		String url = "jdbc:oracle:thin:@f8cloud1129.falabella.cl:1541/arsmxts";
		String user = "USW_BCT";
		String password = "ubct392sK7";
		
		String urlEncriptado = this.encriptar(url);
		String urlUser = this.encriptar(user);
		String urlPassword = this.encriptar(password);
		
		System.out.println("jdbc.bct.url=" + urlEncriptado );
		System.out.println("jdbc.bct.user=" + urlUser);
		System.out.println("jdbc.bct.pass=" + urlPassword);
		
		System.out.println("");
		
		System.out.println("jdbc.bct.url="+this.desencriptar(urlEncriptado));
		System.out.println("jdbc.bct.user="+this.desencriptar(urlUser));
		System.out.println("jdbc.bct.pass="+this.desencriptar(urlPassword));
		
		
	}
	
	//@Test
	public void testPac() {
		String url = "jdbc:sqlserver://10.138.150.74:1433;databaseName=SODIMAC_SAP;encrypt=true;trustServerCertificate=true";
		String user = "SodimacAdm";
		String password = "Pa55word";
		
		String urlEncriptado = this.encriptar(url);
		String urlUser = this.encriptar(user);
		String urlPassword = this.encriptar(password);
		
		System.out.println("jdbc.sap.url=" + urlEncriptado );
		System.out.println("jdbc.sap.user=" + urlUser);
		System.out.println("jdbc.sap.pass=" + urlPassword);
		
		System.out.println("");
		
		System.out.println("jdbc.sap.url="+this.desencriptar(urlEncriptado));
		System.out.println("jdbc.sap.user="+this.desencriptar(urlUser));
		System.out.println("jdbc.sap.pass="+this.desencriptar(urlPassword));
		
		
	}
	
	public String encriptar(String textToEncrypt) {
		
		inicializar();
		if (textToEncrypt.isEmpty()) return "";
		return crypto.encrypt(textToEncrypt);		
	}
		
	public String desencriptar(String encryptedText) {
		
		inicializar();		
		if (encryptedText.isEmpty()) {
			return "";
		}
		return crypto.decrypt(encryptedText);
	}
	
	public void inicializar() {
		
		if (crypto == null) {
			String path = "C:\\config\\cryptodb\\cifrado.properties";
			crypto = new CryptoServiceImpl(path);
		}
	}

}

package com.sodimac.bctfacturacion;

import org.junit.jupiter.api.Test;

import com.sodimac.lib.crypto.service.CryptoService;
import com.sodimac.lib.crypto.service.CryptoServiceImpl;


public class CriptoTest {

	private CryptoService crypto;
	
	@Test
	public void testCesTest() {
		
		String url = "jdbc:sqlserver://10.138.153.10;databaseName=SODIMAC_SAP_DEV;encrypt=true;trustServerCertificate=true";
		String user = "SodimacDevUsr";
		String password = "Pa55wordDev";
		
		String urlEncriptado = this.encriptar(url);
		String urlUser = this.encriptar(user);
		String urlPassword = this.encriptar(password);
		
		System.out.println("jdbc.ces.url=" + urlEncriptado );
		System.out.println("jdbc.ces.user=" + urlUser);
		System.out.println("jdbc.ces.pass=" + urlPassword);
		
		System.out.println("");
		
		System.out.println("jdbc.ces.url="+this.desencriptar(urlEncriptado));
		System.out.println("jdbc.ces.user="+this.desencriptar(urlUser));
		System.out.println("jdbc.ces.pass="+this.desencriptar(urlPassword));
		
		
	}
	
	//@Test
	public void testBct() {
		
//		String url = "jdbc:oracle:thin:@f8cloud1129.falabella.cl:1541/arsmxts";
//		String user = "USW_BCT";
//		String password = "ubct392sK7";
		
		String url = "jdbc:oracle:thin:@ramsay.falabella.cl:1531/arsmxpr";
		String user = "BATSW_FAC";
		String password = "M5R89NJYVS";
		
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
	
	@Test
	public void testPac() {
		
//		String url = "jdbc:sqlserver://10.138.150.124:5319;databaseName=SODIMAC_FISCAL_PROD;encrypt=true;trustServerCertificate=true";
//		String user = "UserBatchFinanzas";
//		String password = "kiTuNs39#m2$qPy2n1";
		
		String url = "jdbc:sqlserver://10.138.153.10:5319;databaseName=SODIMAC_REBATES_DEV;encrypt=true;trustServerCertificate=true";
		String user = "SodimacDevUsr";
		String password = "Pa55wordDev";
		
		String urlEncriptado = this.encriptar(url);
		String urlUser = this.encriptar(user);
		String urlPassword = this.encriptar(password);
		
		System.out.println("");
		System.out.println("jdbc.fiscal.url=" + urlEncriptado );
		System.out.println("jdbc.fiscal.user=" + urlUser);
		System.out.println("jdbc.fiscal.pass=" + urlPassword);
		
		System.out.println("");
		
		System.out.println("jdbc.fiscal.url="+this.desencriptar(urlEncriptado));
		System.out.println("jdbc.fiscal.user="+this.desencriptar(urlUser));
		System.out.println("jdbc.fiscal.pass="+this.desencriptar(urlPassword));
		
		
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

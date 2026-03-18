package com.sodimac.facturacion.cliente.configuracion;

import java.security.KeyManagementException;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;

import javax.net.ssl.SSLContext;

import org.apache.http.conn.ssl.SSLConnectionSocketFactory;
import org.apache.http.conn.ssl.TrustSelfSignedStrategy;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.ssl.SSLContexts;
import org.springframework.stereotype.Component;

import com.mashape.unirest.http.HttpResponse;
import com.mashape.unirest.http.Unirest;
import com.mashape.unirest.http.exceptions.UnirestException;
import com.sodimac.facturacion.exception.ClientException;

@Component
public class ConfiguracionClientImpl implements ConfiguracionClient {

	@Override
	public HttpResponse<String> login(String url, String usuario, String password) throws ClientException {
		HttpResponse<String> response = null;
		try {
			Unirest.setHttpClient(ClientSSl());
			response = Unirest.post(url)
					.header("Content-Type", "application/json")
					.body("{\"username\": \""+usuario+"\",\r\n  \"password\": \""+password+"\"\r\n}")
					.asString();
		} catch (KeyManagementException | NoSuchAlgorithmException | KeyStoreException | UnirestException e) {
			e.printStackTrace();
			throw new ClientException(e); 
		}
		return response;
	}

	@Override
	public HttpResponse<String> post(String url, String headerValue, String body) throws ClientException {
		HttpResponse<String> response = null;
		
		try {
			Unirest.setHttpClient(ClientSSl());
			response = Unirest.post(url)
					.header("Authorization", headerValue)
					.header("Content-Type", "application/json")
					.body(body )
					.asString();
		} catch (KeyManagementException | NoSuchAlgorithmException | KeyStoreException | UnirestException e) {
			e.printStackTrace();
			throw new ClientException(e); 
		}
		return response;
	}
	
	/**
	* Metodo que permite realizar una llamada a un      
	* Servicio HTTPS      
	*/
	private CloseableHttpClient ClientSSl() throws KeyManagementException, NoSuchAlgorithmException, KeyStoreException
	{
		SSLContext sslcontext = SSLContexts.custom()
	            .loadTrustMaterial(null, new TrustSelfSignedStrategy())
	            .build();

	    @SuppressWarnings("deprecation")
		SSLConnectionSocketFactory sslsf = new SSLConnectionSocketFactory(sslcontext,SSLConnectionSocketFactory.ALLOW_ALL_HOSTNAME_VERIFIER);
	    CloseableHttpClient httpclient = HttpClients.custom()
	            .setSSLSocketFactory(sslsf)
	            .build();
	    
	    return httpclient;
	}	

}

package com.sodimac.facturacion.service;

import java.security.KeyManagementException;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.util.List;

import javax.net.ssl.SSLContext;

import org.apache.http.conn.ssl.SSLConnectionSocketFactory;
import org.apache.http.conn.ssl.TrustSelfSignedStrategy;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.ssl.SSLContexts;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JavaType;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.mashape.unirest.http.Headers;
import com.mashape.unirest.http.HttpResponse;
import com.mashape.unirest.http.Unirest;
import com.sodimac.facturacion.clientews.wsprmpar.Parametro;
import com.sodimac.facturacion.clientews.wsprmpar.WsResponse;
import com.sodimac.facturacion.entity.CatConfiguracionEntity;
import com.sodimac.facturacion.repository.CatConfiguracionRepository;

@Service
public class CatConfiguracionServiceImpl implements CatConfiguracionService {

	private Logger logger = LoggerFactory.getLogger(CatConfiguracionServiceImpl.class);
	
	@Autowired
	private CatConfiguracionRepository catConfiguracionRepository;
	
	String UrlLogin = "";
	String userName = "";
	String userPass = "";
	String UrlObtenerParametro = "";
	String headerValue = "";

	@Override
	@Transactional
	public List<CatConfiguracionEntity> getAll() {
		return catConfiguracionRepository.findAll();
	}
	
	@Override
	@Transactional
	public String findParameterByKey(String NombreCampo) {
		String result = "";
		if (NombreCampo.equals("Configuracion.RFC.PublicoGeneral") || NombreCampo.equals("Aplicacion.DiasPermitidosFacturar")) {
			
			WsResponse<Parametro> response = new WsResponse<Parametro>();
			inicializarWsprmfac();
			if (headerValue.isEmpty()) obtenerToken();
			
	    	int contador=0;
			HttpResponse<String> responseObtenerParametro = null;
			ObjectMapper objectMapper = new ObjectMapper();
			String urlGet = UrlObtenerParametro + NombreCampo;
					
			do {
				try {
					responseObtenerParametro = Unirest.get(urlGet)						
						.header("Authorization", headerValue)
						.header("Content-Type", "application/json")
						.asString();
				} catch (Exception e) {
					logger.error("parametro " + NombreCampo + ": ", e);
				}
				
				if (responseObtenerParametro != null) {
					try {
						if (responseObtenerParametro.getStatus() != 401) {
							JavaType javaType = objectMapper.getTypeFactory().constructParametricType(WsResponse.class, Parametro.class);
							response = objectMapper.readValue(responseObtenerParametro.getBody(), javaType);
							if (response.getRespuesta().getCodigo().equals("403")) {
								obtenerToken();
								responseObtenerParametro = null;
								response = new WsResponse<Parametro>();
							}							
						}
					} catch (JsonProcessingException e) {
						logger.error("parametro " + NombreCampo + ": ", e);
					}
					
				}
				contador += 1;

			} while (responseObtenerParametro == null && contador <= 4);
								
			if (response != null && response.getRespuesta().getCodigo().equals("1")) {
				if (response.getData().isActivo()) {
					result = response.getData().getValor().trim();
				} else {
					result = response.getData().getValorInactivo().trim();
				}
			}			
			
		} else {
			result = catConfiguracionRepository.findParameterByKey(NombreCampo); 
		}
		return result; 
	}

	private void inicializarWsprmfac() {
		if (UrlLogin == "") UrlLogin = catConfiguracionRepository.findParameterByKey("WebService.ParametrosFact.Url.Login");
		if (userName == "") userName = catConfiguracionRepository.findParameterByKey("WebService.ParametrosFact.Usuario");
		if (userPass == "") userPass = catConfiguracionRepository.findParameterByKey("WebService.ParametrosFact.Password");
		if (UrlObtenerParametro == "") UrlObtenerParametro = catConfiguracionRepository.findParameterByKey("WebService.ParametrosFact.Url.ObtenerParametro");

		try {
			Unirest.setHttpClient(ClientSSl());
		} catch (KeyManagementException | NoSuchAlgorithmException | KeyStoreException e) {
			e.printStackTrace();
		}
		
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
	
	private void obtenerToken() {
 		int contador = 0;
 		HttpResponse<String> responseLogin = null;
 		
    	do {
			try {
				responseLogin = Unirest.post(UrlLogin)
					.header("Content-Type", "application/json")
					.body("{\"username\": \""+userName+"\",\r\n  \"password\": \""+userPass+"\"\r\n}")
					.asString();		
			} catch (Exception e) {
				logger.error("Token: ", e);
			}
			contador += 1;

		} while (responseLogin == null 
				&& contador <= 4);
		
    	if (responseLogin != null) {
    		Headers headers = responseLogin.getHeaders();
    		headerValue =  headers.getFirst("authorization");				    		
    	}
	}

}

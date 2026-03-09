package com.sodimac.cfdi.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.mashape.unirest.http.Headers;
import com.mashape.unirest.http.HttpResponse;
import com.sodimac.cfdi.client.ConfiguracionClient;
import com.sodimac.cfdi.model.ClientResponseTYPE;
import com.sodimac.cfdi.model.TimbrarComplemento;
import com.sodimac.cfdi.model.payload.BodyTimbrarComplemento;

@Service
public class WsftApiServiceImpl implements WsftApiService {

	private static final Logger logger = LoggerFactory.getLogger(WsftApiServiceImpl.class);
	
	private String UrlLogin;
	private String UrlTimbrarComplementoCfdi;
	private String userName;
	private String userPass;
	private String headerValue;
	
	@Autowired
	private CatConfiguracionService catConfiguracionService;
	
	@Autowired
	private ConfiguracionClient client;
	
	private void inicializarWsft() {
		if (this.UrlLogin == null) {
			this.UrlLogin = this.catConfiguracionService.findParameterByKey("WebService.Facturacion.Url.Login");
			//this.UrlLogin = "http://localhost:8081/api/login";
		}
		if (this.UrlTimbrarComplementoCfdi == null) {
			this.UrlTimbrarComplementoCfdi = this.catConfiguracionService.findParameterByKey("WebService.Facturacion.Url.ComplementoCfdi");
			//this.UrlTimbrarComplementoCfdi = "http://localhost:8081/api/timbrarComplemento";
		}
		if (this.userName == null) {
			this.userName = this.catConfiguracionService.findParameterByKey("WebService.Facturacion.Usuario");
		}
		if (this.userPass == null) {
			this.userPass = this.catConfiguracionService.findParameterByKey("WebService.Facturacion.Password");		
		}
	}
	
	@Override
	public String obtenerToken(String url, String usuario, String password) {
		String headerValue = null;
		int contador = 0;
 		HttpResponse<String> responseLogin = null;
    	do {
			try {
				responseLogin = client.login(url, usuario, password);		
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
		return headerValue;
	}
	
	@Override
	public ClientResponseTYPE<TimbrarComplemento> timbrarComplementoCFDI(Integer idPagoComplemento) {
		ClientResponseTYPE<TimbrarComplemento> response = new ClientResponseTYPE<TimbrarComplemento>();	
		BodyTimbrarComplemento body = new BodyTimbrarComplemento();
		logger.info("idPagoComplemento: " + idPagoComplemento);
		body.setIdTransaccion(String.valueOf(idPagoComplemento));
		this.inicializarWsft();
		
		if (this.headerValue == null) { 
			this.headerValue = this.obtenerToken(this.UrlLogin, this.userName, this.userPass);
		}

    	int contador=0;
		HttpResponse<String> responseHttp = null;
		ObjectMapper objectMapper = new ObjectMapper();
		
		do {
			try {
				String writeValueAsString = objectMapper.writeValueAsString(body);
				logger.info("writeValueAsString: " + writeValueAsString);
				responseHttp = this.client.post(this.UrlTimbrarComplementoCfdi, this.headerValue, writeValueAsString);
			} catch (Exception e) {
				logger.error("Timbrar complemento de pago: " + body.getIdTransaccion() + ": ", e);
			}
			
			if (responseHttp != null) {
				try {
					response = objectMapper.readValue( responseHttp.getBody(), new TypeReference<ClientResponseTYPE<TimbrarComplemento>>() {});
					if (response.getRespuesta().getCodigo().equals("5")) {
						this.headerValue = this.obtenerToken(this.UrlLogin, this.userName, this.userPass);
						responseHttp = null;
						response = new ClientResponseTYPE<TimbrarComplemento>();
					}
				} catch (JsonProcessingException e) {
					logger.error("Consultar versi\u00f3n del CFDI " + body.getIdTransaccion() + ": ", e);
				}
			}
			contador += 1;

		} while (responseHttp == null && contador <= 4);
		return response;
	}	
}

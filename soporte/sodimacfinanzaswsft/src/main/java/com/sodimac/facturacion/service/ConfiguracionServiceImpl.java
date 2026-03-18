package com.sodimac.facturacion.service;

import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.mashape.unirest.http.Headers;
import com.mashape.unirest.http.HttpResponse;
import com.sodimac.facturacion.cliente.configuracion.ConfiguracionClient;
import com.sodimac.facturacion.cliente.ws.model.ClientResponseTYPE;
import com.sodimac.facturacion.models.Mes;
import com.sodimac.facturacion.models.Periodicidad;
import com.sodimac.facturacion.models.UsoDeCfdi;
import com.sodimac.facturacion.payload.BodyMes;
import com.sodimac.facturacion.payload.BodyPeriodicidad;
import com.sodimac.facturacion.payload.BodyUsoDeCfdi40;

@Service
public class ConfiguracionServiceImpl implements ConfiguracionService {

	private static final Logger logger = LoggerFactory.getLogger(ConfiguracionServiceImpl.class);
	
	@Autowired
	private ConfiguracionClient client;
	
	@Autowired
	private ConfiguracionFacturacionService configFacService;
	
	private String UrlLogin;
	private String UrlConsultaMes;
	private String UrlConsultaPeriodicidad;
	private String userName;
	private String userPass;
	private String headerValue;
	private String UrlConsultaTodosUsoCfdi33;
	private String UrlConsultaTodosUsoCfdi40;
	
	private void inicializarWsft() {
		if (this.UrlLogin == null) {
			this.UrlLogin = this.configFacService.getConfig().get("WS.Configuracion.Url.Login");
		}
		if (this.userName == null) {
			this.userName = this.configFacService.getConfig().get("WS.Configuracion.Usuario");
		}
		if (this.userPass == null) {
			this.userPass = this.configFacService.getConfig().get("WS.Configuracion.Password");		
		}
		if (this.UrlConsultaMes == null) {
			this.UrlConsultaMes = this.configFacService.getConfig().get("WS.Configuracion.Url.ConsultaMes");
		}
		if (this.UrlConsultaPeriodicidad == null) {
			this.UrlConsultaPeriodicidad = this.configFacService.getConfig().get("WS.Configuracion.Url.ConsultaPeriodicidad");
		}
		if (this.UrlConsultaTodosUsoCfdi33 == null) {
			this.UrlConsultaTodosUsoCfdi33 = this.configFacService.getConfig().get("WS.Configuracion.Url.UrlConsultaTodosUsoCfdi33");
		}
		if (this.UrlConsultaTodosUsoCfdi40 == null) {
			this.UrlConsultaTodosUsoCfdi40 = this.configFacService.getConfig().get("WS.Configuracion.Url.UrlConsultaTodosUsoCfdi40");
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
	public ClientResponseTYPE<Mes> consultarMes(String clave) {
		ClientResponseTYPE<Mes> response = new ClientResponseTYPE<Mes>();	
		BodyMes body = new BodyMes();
		
		body.setClave(clave);
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
				responseHttp = this.client.post(this.UrlConsultaMes, this.headerValue, writeValueAsString);
			} catch (Exception e) {
				logger.error("Error al consultar Mes" + body.getClave()+ ": ", e);
			}
			
			if (responseHttp != null) {
				try {
					response = objectMapper.readValue(responseHttp.getBody(), new TypeReference<ClientResponseTYPE<Mes>>() {});
					if (response.getRespuesta().getCodigo().equals("5")) {
						this.headerValue = this.obtenerToken(this.UrlLogin, this.userName, this.userPass);
						responseHttp = null;
						response = new ClientResponseTYPE<Mes>();
					}
				} catch (JsonProcessingException e) {
					logger.error("Consultar Mes " + body.getClave() + ": ", e);
				}
			}
			contador += 1;

		} while (responseHttp == null && contador <= 4);
		
		if (response.getRespuesta() == null) {
			logger.error("Error al consultar Mes - Servicio no disponible" + body.getClave()); 
		}
		return response;
	}

	@Override
	public ClientResponseTYPE<Periodicidad> consultarPeriodicidad(String clave) {
		ClientResponseTYPE<Periodicidad> response = new ClientResponseTYPE<Periodicidad>();	
		BodyPeriodicidad body = new BodyPeriodicidad();
		
		body.setClave(clave);
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
				responseHttp = this.client.post(this.UrlConsultaPeriodicidad, this.headerValue, writeValueAsString);
			} catch (Exception e) {
				logger.error("Error al consultar Periodicidad: " + body.getClave()+ ": ", e);
			}
			
			if (responseHttp != null) {
				try {
					response = objectMapper.readValue(responseHttp.getBody(), new TypeReference<ClientResponseTYPE<Periodicidad>>() {});
					if (response.getRespuesta().getCodigo().equals("5")) {
						this.headerValue = this.obtenerToken(this.UrlLogin, this.userName, this.userPass);
						responseHttp = null;
						response = new ClientResponseTYPE<Periodicidad>();
					}
				} catch (JsonProcessingException e) {
					logger.error("Consultar periodicidad: " + body.getClave() + ": ", e);
				}
			}
			contador += 1;

		} while (responseHttp == null && contador <= 4);
		
		if (response.getRespuesta() == null) {
			logger.error("Error al consultar Periodicidad - Servicio no disponible" + body.getClave());
		}
		return response;
	}

	@Override
	public ClientResponseTYPE<List<UsoDeCfdi>> consultarUsoCfdi33All() {
		ClientResponseTYPE<List<UsoDeCfdi>> response = new ClientResponseTYPE<List<UsoDeCfdi>>();	
		BodyUsoDeCfdi40 body = new BodyUsoDeCfdi40();
		this.inicializarWsft();
		
		if (this.headerValue == null) { 
			this.headerValue = this.obtenerToken(this.UrlLogin, this.userName, this.userPass);
		}

    	int contador=0;
		HttpResponse<String> responseHttp = null;
		ObjectMapper objectMapper = new ObjectMapper();
		
		do {
			String writeValueAsString = null;
			try {
				writeValueAsString = objectMapper.writeValueAsString(body);
				responseHttp = this.client.post(this.UrlConsultaTodosUsoCfdi33, this.headerValue, writeValueAsString);
			} catch (Exception e) {
				logger.error("Consulta catalogo de uso de CFDI 3.3: " + writeValueAsString, e);
			}
			
			if (responseHttp != null) {
				try {
					response = objectMapper.readValue(responseHttp.getBody(), new TypeReference<ClientResponseTYPE<List<UsoDeCfdi>>>() {});
					if (response.getRespuesta().getCodigo().equals("5")) {
						this.headerValue = this.obtenerToken(this.UrlLogin, this.userName, this.userPass);
						responseHttp = null;
						response = new ClientResponseTYPE<List<UsoDeCfdi>>();
					}
				} catch (JsonProcessingException e) {
					logger.error("Consulta catalogo de uso de CFDI 3.3: " + writeValueAsString, e);
				}
			}
			contador += 1;

		} while (responseHttp == null && contador <= 4);
		
		if (response.getRespuesta() == null) {
			logger.error("Error al consultar catálogos en versión 3.3");
		}
		return response;
	}
	
	@Override
	public ClientResponseTYPE<List<UsoDeCfdi>> consultarUsoCfdi40All() {
		ClientResponseTYPE<List<UsoDeCfdi>> response = new ClientResponseTYPE<List<UsoDeCfdi>>();	
		BodyUsoDeCfdi40 body = new BodyUsoDeCfdi40();
		
		this.inicializarWsft();
		
		if (this.headerValue == null) { 
			this.headerValue = this.obtenerToken(this.UrlLogin, this.userName, this.userPass);
		}

    	int contador=0;
		HttpResponse<String> responseHttp = null;
		ObjectMapper objectMapper = new ObjectMapper();
		
		do {
			String writeValueAsString = null;
			try {
				writeValueAsString = objectMapper.writeValueAsString(body);
				responseHttp = this.client.post(this.UrlConsultaTodosUsoCfdi40, this.headerValue, writeValueAsString);
			} catch (Exception e) {
				logger.error("Consulta catálogo de usos de CFDI 4.0: " + writeValueAsString, e);
			}
			
			if (responseHttp != null) {
				try {
					response = objectMapper.readValue(responseHttp.getBody(), new TypeReference<ClientResponseTYPE<List<UsoDeCfdi>>>() {});
					if (response.getRespuesta().getCodigo().equals("5")) {
						this.headerValue = this.obtenerToken(this.UrlLogin, this.userName, this.userPass);
						responseHttp = null;
						response = new ClientResponseTYPE<List<UsoDeCfdi>>();
					}
				} catch (JsonProcessingException e) {
					logger.error("Consulta catálogo de usos de CFDI 4.0: " + writeValueAsString, e);
				}
			}
			contador += 1;

		} while (responseHttp == null && contador <= 4);
		
		if (response.getRespuesta() == null) {
			logger.error("Error al consultar catálogos en versión 4.0");
		}
		return response;
	}

}

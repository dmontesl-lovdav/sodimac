package com.sodimac.cfdi.service;

import java.util.List;
import java.util.stream.Collectors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.mashape.unirest.http.Headers;
import com.mashape.unirest.http.HttpResponse;
import com.sodimac.cfdi.client.ConfiguracionClient;
import com.sodimac.cfdi.cliente.wsadministracion.CatCodigoPostalDtoVM;
import com.sodimac.cfdi.cliente.wsadministracion.CatTipoTiendaDtoVM;
import com.sodimac.cfdi.cliente.wsadministracion.ConfDatosEmisorDtoVM;
import com.sodimac.cfdi.cliente.wsadministracion.ConfDatosEmisorTiendaDtoVM;
import com.sodimac.cfdi.clientews.wsft.BodyUsoDeCfdi40;
import com.sodimac.cfdi.model.ClientResponseTYPE;
import com.sodimac.cfdi.model.RespuestaClient;
import com.sodimac.cfdi.models.UsoDeCfdi;


@Service
public class ConfiguracionServiceImpl implements ConfiguracionService {

	private static final Logger logger = LoggerFactory.getLogger(ConfiguracionServiceImpl.class);
	
	@Autowired
	private ConfiguracionClient client;
	
	@Autowired
	private CatConfiguracionService catConfiguracionService;
	
	private String UrlLogin;
	private String UrlConsultaTodosUsoCfdi33;
	private String UrlConsultaTodosUsoCfdi40;
	private String userName;
	private String userPass;
	private String headerValue;
	private String UrlconfdatosemisortiendaFindAll;
	private String UrlconfdatosemisortiendaCreate;
	private String UrlconfdatosemisortiendaUpdate;
	private String UrlcatTipoTiendaFindAll;
	private String UrlconfdatosemisorFindAll;
	private String UrlcatCodigoPostalFindById;
	
	private void inicializarWsft() {
		if (this.UrlLogin == null) {
			this.UrlLogin = this.catConfiguracionService.findParameterByKey("WS.Configuracion.Url.Login");
		}
		if (this.UrlConsultaTodosUsoCfdi33 == null) {
			this.UrlConsultaTodosUsoCfdi33 = this.catConfiguracionService.findParameterByKey("WS.Configuracion.Url.UrlConsultaTodosUsoCfdi33");
		}
		if (this.UrlConsultaTodosUsoCfdi40 == null) {
			this.UrlConsultaTodosUsoCfdi40 = this.catConfiguracionService.findParameterByKey("WS.Configuracion.Url.UrlConsultaTodosUsoCfdi40");
		}
		if (this.userName == null) {
			this.userName = this.catConfiguracionService.findParameterByKey("WS.Configuracion.Usuario");
		}
		if (this.userPass == null) {
			this.userPass = this.catConfiguracionService.findParameterByKey("WS.Configuracion.Password");
		}
		if (this.UrlconfdatosemisortiendaFindAll == null) {
			this.UrlconfdatosemisortiendaFindAll = this.catConfiguracionService.findParameterByKey("WS.Configuracion.Url.UrlconfdatosemisortiendaFindAll");
		}
		if (this.UrlconfdatosemisortiendaCreate == null) {
			this.UrlconfdatosemisortiendaCreate = this.catConfiguracionService.findParameterByKey("WS.Configuracion.Url.UrlconfdatosemisortiendaCreate");
		}
		if (this.UrlconfdatosemisortiendaUpdate == null) {
			this.UrlconfdatosemisortiendaUpdate = this.catConfiguracionService.findParameterByKey("WS.Configuracion.Url.UrlconfdatosemisortiendaUpdate");
		}
		if (this.UrlcatTipoTiendaFindAll == null) {
			this.UrlcatTipoTiendaFindAll = this.catConfiguracionService.findParameterByKey("WS.Configuracion.Url.UrlcatTipoTiendaFindAll");
		}
		if (this.UrlconfdatosemisorFindAll == null) {
			this.UrlconfdatosemisorFindAll = this.catConfiguracionService.findParameterByKey("WS.Configuracion.Url.UrlconfdatosemisorFindAll");
		}
		
		if (this.UrlcatCodigoPostalFindById == null) {
			this.UrlcatCodigoPostalFindById = this.catConfiguracionService.findParameterByKey("WS.Configuracion.Url.UrlcatCodigoPostalFindById");
		}
		
		//this.UrlconfdatosemisortiendaFindAll = "http://localhost:8080/finanzasadminfacturacion/api/catologo/confdatosemisortienda/findAll";
		//this.UrlconfdatosemisortiendaUpdate = "http://localhost:8080/finanzasadminfacturacion/api/catologo/confdatosemisortienda/update";
		//this.UrlconfdatosemisortiendaCreate = "http://localhost:8080/finanzasadminfacturacion/api/catologo/confdatosemisortienda/create";
		//this.UrlcatTipoTiendaFindAll = "http://localhost:8080/finanzasadminfacturacion/api/catologo/cattipotienda/findAll";
		//this.UrlconfdatosemisorFindAll = "http://localhost:8080/finanzasadminfacturacion/api/catologo/confdatosemisor/findAll";
		//this.UrlcatCodigoPostalFindById = "http://localhost:8080/finanzasadminfacturacion/api/catologo/catcodigopostal/findById";
		//this.UrlLogin = "http://localhost:8080/finanzasadminfacturacion/api/login";
		/**this.UrlValidaCP = "http://localhost:8081/api/codigopostal";
		this.UrlConsultaTodosUsoCfdi33 = "http://localhost:8081/api/usoscfdi33/all";
		this.UrlConsultaTodosUsoCfdi40 = "http://localhost:8081/api/usoscfdi40/all";*/
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

	@Override
	public ClientResponseTYPE<List<ConfDatosEmisorTiendaDtoVM>> consultaConfDatosEmisorTiendaAll() {
		ClientResponseTYPE<List<ConfDatosEmisorTiendaDtoVM>> response = new ClientResponseTYPE<List<ConfDatosEmisorTiendaDtoVM>>();
		this.inicializarWsft();
		
		if (this.headerValue == null) { 
			this.headerValue = this.obtenerToken(this.UrlLogin, this.userName, this.userPass);
		}
		
		
    	int contador=0;
		HttpResponse<String> responseHttp = null;
		ObjectMapper objectMapper = new ObjectMapper();
		
		do {
			try {
				responseHttp = this.client.get(this.UrlconfdatosemisortiendaFindAll, this.headerValue);
			} catch (Exception e) {
				logger.error("Consulta Catalogo de ConfDatosEmisorTienda" , e);
			}
			
			if (responseHttp != null) {
				try {
					response = objectMapper.readValue(responseHttp.getBody(), new TypeReference<ClientResponseTYPE<List<ConfDatosEmisorTiendaDtoVM>>>() {});
					if (response.getRespuesta().getCodigo().equals("5")) {
						this.headerValue = this.obtenerToken(this.UrlLogin, this.userName, this.userPass);
						responseHttp = null;
						response = new ClientResponseTYPE<List<ConfDatosEmisorTiendaDtoVM>>();
					}
				} catch (JsonProcessingException e) {
					logger.error("Consulta Catalogo de ConfDatosEmisorTienda: ", e);
				}
			}
			contador += 1;

		} while (responseHttp == null && contador <= 4);
		
		if (response.getRespuesta() == null) {
			logger.error("Consulta Catalogo de ConfDatosEmisorTienda");
		}
		return response;
	}

	@Override
	public ClientResponseTYPE<String> consultaConfDatosEmisorTiendaUpdate(ConfDatosEmisorTiendaDtoVM body) {
		ClientResponseTYPE<String> response = new ClientResponseTYPE<String>();
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
				responseHttp = this.client.post(this.UrlconfdatosemisortiendaUpdate, this.headerValue, writeValueAsString);
			} catch (Exception e) {
				logger.error("Actualiza confDatosEmisorTienda " + writeValueAsString, e);
			}
			
			if (responseHttp != null) {
				try {
					response = objectMapper.readValue(responseHttp.getBody(), new TypeReference<ClientResponseTYPE<String>>() {});
					if (response.getRespuesta().getCodigo().equals("5")) {
						this.headerValue = this.obtenerToken(this.UrlLogin, this.userName, this.userPass);
						responseHttp = null;
						response = new ClientResponseTYPE<String>();
					}
				} catch (JsonProcessingException e) {
					logger.error("Actualiza confDatosEmisorTienda " + writeValueAsString, e);
				}
			}
			contador += 1;

		} while (responseHttp == null && contador <= 4);
		
		if (response.getRespuesta() == null) {
			logger.error("Actualiza confDatosEmisorTienda");
		}
		return response;
		
	}

	@Override
	public ClientResponseTYPE<String> consultaConfDatosEmisorTiendaCreate(ConfDatosEmisorTiendaDtoVM body) {
		ClientResponseTYPE<String> response = new ClientResponseTYPE<String>();
		List<ConfDatosEmisorTiendaDtoVM> tiendaAll = consultaConfDatosEmisorTiendaAll().getData();
		long existe = tiendaAll.stream().filter(p -> p.getIdTienda().equals(body.getIdTienda())).collect(Collectors.toList()).size();
		
		if(existe > 0) {
			response.setRespuesta(new RespuestaClient("0", "La Tienda: " + body.getIdTienda() + " ya existe"));
		} else {
		
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
					responseHttp = this.client.post(this.UrlconfdatosemisortiendaCreate, this.headerValue, writeValueAsString);
				} catch (Exception e) {
					logger.error("Crea confDatosEmisorTienda " + writeValueAsString, e);
				}
				
				if (responseHttp != null) {
					try {
						response = objectMapper.readValue(responseHttp.getBody(), new TypeReference<ClientResponseTYPE<String>>() {});
						if (response.getRespuesta().getCodigo().equals("5")) {
							this.headerValue = this.obtenerToken(this.UrlLogin, this.userName, this.userPass);
							responseHttp = null;
							response = new ClientResponseTYPE<String>();
						}
					} catch (JsonProcessingException e) {
						logger.error("Crea confDatosEmisorTienda " + writeValueAsString, e);
					}
				}
				contador += 1;
	
			} while (responseHttp == null && contador <= 4);
			
			if (response.getRespuesta() == null) {
				logger.error("Crea confDatosEmisorTienda");
			}
		}
		return response;
		
	}

	@Override
	public ClientResponseTYPE<List<CatTipoTiendaDtoVM>> catTipoTiendaFindAll() {
		ClientResponseTYPE<List<CatTipoTiendaDtoVM>> response = new ClientResponseTYPE<List<CatTipoTiendaDtoVM>>();
		this.inicializarWsft();
		
		if (this.headerValue == null) { 
			this.headerValue = this.obtenerToken(this.UrlLogin, this.userName, this.userPass);
		}
		
		
    	int contador=0;
		HttpResponse<String> responseHttp = null;
		ObjectMapper objectMapper = new ObjectMapper();
		
		do {
			try {
				responseHttp = this.client.get(this.UrlcatTipoTiendaFindAll, this.headerValue);
			} catch (Exception e) {
				logger.error("Consulta Catalogo de CatitipoTienda" , e);
			}
			
			if (responseHttp != null) {
				try {
					response = objectMapper.readValue(responseHttp.getBody(), new TypeReference<ClientResponseTYPE<List<CatTipoTiendaDtoVM>>>() {});
					if (response.getRespuesta().getCodigo().equals("5")) {
						this.headerValue = this.obtenerToken(this.UrlLogin, this.userName, this.userPass);
						responseHttp = null;
						response = new ClientResponseTYPE<List<CatTipoTiendaDtoVM>>();
					}
				} catch (JsonProcessingException e) {
					logger.error("Consulta Catalogo de CatitipoTienda: ", e);
				}
			}
			contador += 1;

		} while (responseHttp == null && contador <= 4);
		
		if (response.getRespuesta() == null) {
			logger.error("Consulta Catalogo de CatitipoTienda");
		}
		return response;
	}

	@Override
	public ClientResponseTYPE<List<ConfDatosEmisorDtoVM>> consultaConfDatosEmisorAll() {
		ClientResponseTYPE<List<ConfDatosEmisorDtoVM>> response = new ClientResponseTYPE<List<ConfDatosEmisorDtoVM>>();
		this.inicializarWsft();
		
		if (this.headerValue == null) { 
			this.headerValue = this.obtenerToken(this.UrlLogin, this.userName, this.userPass);
		}
		
		
    	int contador=0;
		HttpResponse<String> responseHttp = null;
		ObjectMapper objectMapper = new ObjectMapper();
		objectMapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
		
		do {
			try {
				responseHttp = this.client.get(this.UrlconfdatosemisorFindAll, this.headerValue);
			} catch (Exception e) {
				logger.error("Consulta Catalogo de CatitipoTienda" , e);
			}
			
			if (responseHttp != null) {
				try {
					response = objectMapper.readValue(responseHttp.getBody(), new TypeReference<ClientResponseTYPE<List<ConfDatosEmisorDtoVM>>>() {});
					if (response.getRespuesta().getCodigo().equals("5")) {
						this.headerValue = this.obtenerToken(this.UrlLogin, this.userName, this.userPass);
						responseHttp = null;
						response = new ClientResponseTYPE<List<ConfDatosEmisorDtoVM>>();
					}
				} catch (JsonProcessingException e) {
					logger.error("Consulta Catalogo de ConfDatosEmisor: ", e);
				}
			}
			contador += 1;

		} while (responseHttp == null && contador <= 4);
		
		if (response.getRespuesta() == null) {
			logger.error("Consulta Catalogo de ConfDatosEmisor");
		}
		return response;
	}

	@Override
	public ClientResponseTYPE<CatCodigoPostalDtoVM> consultaCodigoPostalById(Integer cp) {
		ClientResponseTYPE<CatCodigoPostalDtoVM> response = new ClientResponseTYPE<CatCodigoPostalDtoVM>();
		this.inicializarWsft();
		
		if (this.headerValue == null) { 
			this.headerValue = this.obtenerToken(this.UrlLogin, this.userName, this.userPass);
		}
		
		
    	int contador=0;
		HttpResponse<String> responseHttp = null;
		ObjectMapper objectMapper = new ObjectMapper();
		objectMapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
		
		do {
			try {
				responseHttp = this.client.get(this.UrlcatCodigoPostalFindById + "/" + cp, this.headerValue);
			} catch (Exception e) {
				logger.error("Consulta Catalogo de CatitipoTienda" , e);
			}
			
			if (responseHttp != null) {
				try {
					response = objectMapper.readValue(responseHttp.getBody(), new TypeReference<ClientResponseTYPE<CatCodigoPostalDtoVM>>() {});
					if (response.getRespuesta().getCodigo().equals("5")) {
						this.headerValue = this.obtenerToken(this.UrlLogin, this.userName, this.userPass);
						responseHttp = null;
						response = new ClientResponseTYPE<CatCodigoPostalDtoVM>();
					}
				} catch (JsonProcessingException e) {
					logger.error("Consulta Catalogo de CatCodigoPostal: ", e);
				}
			}
			contador += 1;

		} while (responseHttp == null && contador <= 4);
		
		if (response.getRespuesta() == null) {
			logger.error("Consulta Catalogo de CatCodigoPostal");
		}
		return response;
	}
	
}

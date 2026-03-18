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
import com.sodimac.facturacion.clientews.configuracion.ConfiguracionClient;
import com.sodimac.facturacion.clientews.model.ClientResponseTYPE;
import com.sodimac.facturacion.clientews.payload.BodyCodigoPostal;
import com.sodimac.facturacion.clientews.payload.BodyRegimenCapital;
import com.sodimac.facturacion.clientews.payload.BodyRegimenFiscal;
import com.sodimac.facturacion.clientews.payload.BodyUsoDeCfdi40;
import com.sodimac.facturacion.clientews.payload.BodyUsoDeCfdi33;
import com.sodimac.facturacion.clientews.payload.BodyVersionCfdi;
import com.sodimac.facturacion.models.CodigoPostal;
import com.sodimac.facturacion.models.ListaRegimenFiscal;
import com.sodimac.facturacion.models.RegimenCapital;
import com.sodimac.facturacion.models.UsoDeCfdi;
import com.sodimac.facturacion.models.VersionCfdi;
import com.sodimac.facturacion.util.UtilsApi;
import com.sodimac.facturacion.util.enums.ECodigo;

@Service
public class ConfiguracionServiceImpl implements ConfiguracionService {

	private static final Logger logger = LoggerFactory.getLogger(ConfiguracionServiceImpl.class);
	
	@Autowired
	private ConfiguracionClient client;
	
	@Autowired
	private CatConfiguracionService catConfiguracionService;
	
	private String UrlLogin;
	private String UrlValidaCP;
	private String UrlConsultaRegimenFiscal;
	private String UrlConsultaVersionCfdi;
	private String UrlValidaRegimenCapital;
	private String UrlConsultaUsoCfdi;
	private String UrlConsultaUsoCfdi33;
	private String UrlConsultaUsoCfdi40;
	private String UrlConsultaTodosUsoCfdi33;
	private String UrlConsultaTodosUsoCfdi40;
	private String userName;
	private String userPass;
	private String headerValue;
	private List<UsoDeCfdi> listaUsoCfdi;
	
	private void inicializarWsft() {
		if (this.UrlLogin == null) {
			this.UrlLogin = this.catConfiguracionService.findParameterByKey("WS.Configuracion.Url.Login");
		}
		if (this.UrlValidaCP == null) {
			this.UrlValidaCP = this.catConfiguracionService.findParameterByKey("WS.Configuracion.Url.ValidaCP");
		}
		if (this.UrlConsultaRegimenFiscal == null) {
			this.UrlConsultaRegimenFiscal = this.catConfiguracionService.findParameterByKey("WS.Configuracion.Url.RegimenFiscal");
		}
		if (this.UrlConsultaVersionCfdi == null) {
			this.UrlConsultaVersionCfdi = this.catConfiguracionService.findParameterByKey("WS.Configuracion.Url.VersionCfdi");
		}
		if (this.UrlValidaRegimenCapital == null) {
			this.UrlValidaRegimenCapital = this.catConfiguracionService.findParameterByKey("WS.Configuracion.Url.ValidaRegimenCapital");
		}
		if (this.UrlConsultaUsoCfdi == null) {
			this.UrlConsultaUsoCfdi = this.catConfiguracionService.findParameterByKey("WS.Configuracion.Url.UrlConsultaUsoCfdi");
		}
		if (this.UrlConsultaUsoCfdi33 == null) {
			this.UrlConsultaUsoCfdi33 = this.catConfiguracionService.findParameterByKey("WS.Configuracion.Url.UrlConsultaUsoCfdi33");
		}
		if (this.UrlConsultaUsoCfdi40 == null) {
			this.UrlConsultaUsoCfdi40 = this.catConfiguracionService.findParameterByKey("WS.Configuracion.Url.UrlConsultaUsoCfdi40");
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
		
		/*this.UrlLogin = "http://localhost:8081/api/login";
		this.UrlValidaCP = "http://localhost:8081/api/codigopostal";
		this.UrlConsultaRegimenFiscal = "http://localhost:8081/api/regimenfiscal";
		this.UrlConsultaVersionCfdi = "http://localhost:8081/api/versiontimbrado";
		this.UrlValidaRegimenCapital = "http://localhost:8081/api/regimendecapital";
		this.UrlConsultaUsoCfdi = "http://localhost:8081/api/usoscfdi";
		this.UrlConsultaUsoCfdi33 = "http://localhost:8081/api/usoscfdi33";
		this.UrlConsultaUsoCfdi40 = "http://localhost:8081/api/usoscfdi40";
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
	public ClientResponseTYPE<CodigoPostal> consultarCodigoPostal(String cp) {
		ClientResponseTYPE<CodigoPostal> response = new ClientResponseTYPE<CodigoPostal>();	
		BodyCodigoPostal body = new BodyCodigoPostal();
		
		body.setCodigoPostal(cp);
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
				responseHttp = this.client.post(this.UrlValidaCP, this.headerValue, writeValueAsString);
			} catch (Exception e) {
				logger.error("Validar C\u00f3digo Postal " + body.getCodigoPostal() + ": ", e);
			}
			
			if (responseHttp != null) {
				try {
					response = objectMapper.readValue(responseHttp.getBody(), new TypeReference<ClientResponseTYPE<CodigoPostal>>() {});
					if (response.getRespuesta().getCodigo().equals("5")) {
						this.headerValue = this.obtenerToken(this.UrlLogin, this.userName, this.userPass);
						responseHttp = null;
						response = new ClientResponseTYPE<CodigoPostal>();
					}
				} catch (JsonProcessingException e) {
					logger.error("Validar C\u00f3digo Postal " + body.getCodigoPostal() + ": ", e);
				}
			}
			contador += 1;

		} while (responseHttp == null && contador <= 4);
		
		if (response.getRespuesta() == null) {
			UtilsApi.setRespuesta(response, ECodigo.Error.getValor());
		}
		return response;
	}
	

	@Override
	public ClientResponseTYPE<ListaRegimenFiscal> consultarRegimenFiscal(Integer idTipoPersona) {
		ClientResponseTYPE<ListaRegimenFiscal> response = new ClientResponseTYPE<ListaRegimenFiscal>();	
		BodyRegimenFiscal body = new BodyRegimenFiscal();
		
		body.setIdTipoPersona(idTipoPersona.intValue());
		this.inicializarWsft();
		
		if (this.headerValue == null) { 
			this.headerValue = this.obtenerToken(this.UrlLogin, this.userName, this.userPass);
		}

		System.out.println("this.headerValue:" + this.headerValue);
    	int contador=0;
		HttpResponse<String> responseHttp = null;
		ObjectMapper objectMapper = new ObjectMapper();
		
		do {
			try {
				String writeValueAsString = objectMapper.writeValueAsString(body);
				responseHttp = this.client.post(this.UrlConsultaRegimenFiscal, this.headerValue, writeValueAsString);
			} catch (Exception e) {
				logger.error("Consultar r\u00e9gimen fiscal " + body.getIdTipoPersona() + ": ", e);
			}
			
			if (responseHttp != null) {
				try {
					response = objectMapper.readValue(responseHttp.getBody(), new TypeReference<ClientResponseTYPE<ListaRegimenFiscal>>() {});
					if (response.getRespuesta().getCodigo().equals("5")) {
						this.headerValue = this.obtenerToken(this.UrlLogin, this.userName, this.userPass);
						responseHttp = null;
						response = new ClientResponseTYPE<ListaRegimenFiscal>();
					}
				} catch (JsonProcessingException e) {
					logger.error("R\u00e9gimen Fiscal" + body.getIdTipoPersona() + ": ", e);
				}
			}
			contador += 1;

		} while (responseHttp == null && contador <= 4);
							
		if (response.getRespuesta() == null) {
			UtilsApi.setRespuesta(response, ECodigo.Error.getValor());
		}
		return response;
	}

	@Override
	public ClientResponseTYPE<VersionCfdi> consultarVersionCFDI(Integer idAplicacion) {
		ClientResponseTYPE<VersionCfdi> response = new ClientResponseTYPE<VersionCfdi>();	
		BodyVersionCfdi body = new BodyVersionCfdi();
		logger.info("idAplicacion: " + idAplicacion);
		body.setIdAplicacion(idAplicacion);
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
				responseHttp = this.client.post(this.UrlConsultaVersionCfdi, this.headerValue, writeValueAsString);
			} catch (Exception e) {
				logger.error("Consultar versi\u00f3n del CFDI " + body.getIdAplicacion() + ": ", e);
			}
			
			if (responseHttp != null) {
				try {
					response = objectMapper.readValue( responseHttp.getBody(), new TypeReference<ClientResponseTYPE<VersionCfdi>>() {});
					if (response.getRespuesta().getCodigo().equals("5")) {
						this.headerValue = this.obtenerToken(this.UrlLogin, this.userName, this.userPass);
						responseHttp = null;
						response = new ClientResponseTYPE<VersionCfdi>();
					}
				} catch (JsonProcessingException e) {
					logger.error("Consultar versi\u00f3n del CFDI " + body.getIdAplicacion() + ": ", e);
				}
			}
			contador += 1;

		} while (responseHttp == null && contador <= 4);
							
		if (response.getRespuesta() == null) {
			UtilsApi.setRespuesta(response, ECodigo.Error.getValor());
		}
		return response;
	}

	@Override
	public ClientResponseTYPE<RegimenCapital> validarRegimenCapital(String razonSocial) {
		ClientResponseTYPE<RegimenCapital> response = new ClientResponseTYPE<RegimenCapital>();	
		BodyRegimenCapital body = new BodyRegimenCapital();
		
		body.setRazonSocial(razonSocial);
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
				responseHttp = this.client.post(this.UrlValidaRegimenCapital, this.headerValue, writeValueAsString);
			} catch (Exception e) {
				logger.error("Validar regimen de capital" + body.getRazonSocial() + ": ", e);
			}
			
			if (responseHttp != null) {
				try {
					response = objectMapper.readValue(responseHttp.getBody(), new TypeReference<ClientResponseTYPE<RegimenCapital>>() {});
					if (response.getRespuesta().getCodigo().equals("5")) {
						this.headerValue = this.obtenerToken(this.UrlLogin, this.userName, this.userPass);
						responseHttp = null;
						response = new ClientResponseTYPE<RegimenCapital>();
					}
				} catch (JsonProcessingException e) {
					logger.error("Validar regimen de capital" + body.getRazonSocial() + ": ", e);
				}
			}
			contador += 1;

		} while (responseHttp == null && contador <= 4);
		
		if (response.getRespuesta() == null) {
			UtilsApi.setRespuesta(response, ECodigo.Error.getValor());
		}
		return response;
	}
	
	@Override
	public List<UsoDeCfdi> consultarUsoCfdiInstance() {
		if (this.listaUsoCfdi != null) {
			return this.listaUsoCfdi;
		} else {
			ClientResponseTYPE<List<UsoDeCfdi>> listUsoCfdiResp = this.consultarUsoCfdi();
			if (listUsoCfdiResp.getRespuesta().getCodigo().equals("1")) {
				this.listaUsoCfdi = listUsoCfdiResp.getData();
			}
			return this.listaUsoCfdi;
		}
	}

	@Override
	public ClientResponseTYPE<List<UsoDeCfdi>> consultarUsoCfdi() {
		
		ClientResponseTYPE<List<UsoDeCfdi>> response = new ClientResponseTYPE<List<UsoDeCfdi>>();
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
				responseHttp = this.client.post(this.UrlConsultaUsoCfdi, this.headerValue, writeValueAsString);
			} catch (Exception e) {
				logger.error("Consulta uso de CFDI", e);
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
					logger.error("Consulta uso de CFDI", e);
				}
			}
			contador += 1;

		} while (responseHttp == null && contador <= 4);
		
		if (response.getRespuesta() == null) {
			UtilsApi.setRespuesta(response, ECodigo.Error.getValor());
		}
		return response;
	}
	
	@Override
	public ClientResponseTYPE<List<UsoDeCfdi>> consultarUsoCfdi40(Integer idTipoPersona, String regimenFiscal) {
		ClientResponseTYPE<List<UsoDeCfdi>> response = new ClientResponseTYPE<List<UsoDeCfdi>>();	
		BodyUsoDeCfdi40 body = new BodyUsoDeCfdi40();
		
		body.setIdTipoPersona(idTipoPersona);
		body.setRegimenFiscal(regimenFiscal);
		
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
				responseHttp = this.client.post(this.UrlConsultaUsoCfdi40, this.headerValue, writeValueAsString);
			} catch (Exception e) {
				logger.error("Consulta uso de CFDI por regimen fiscal: " + writeValueAsString, e);
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
					logger.error("Consulta uso de CFDI por regimen fiscal: " + writeValueAsString, e);
				}
			}
			contador += 1;

		} while (responseHttp == null && contador <= 4);
		
		if (response.getRespuesta() == null) {
			UtilsApi.setRespuesta(response, ECodigo.Error.getValor());
		}
		return response;
	}

	@Override
	public ClientResponseTYPE<List<UsoDeCfdi>> consultarUsoCfdi33(Integer idTipoPersona) {
		ClientResponseTYPE<List<UsoDeCfdi>> response = new ClientResponseTYPE<List<UsoDeCfdi>>();	
		BodyUsoDeCfdi33 body = new BodyUsoDeCfdi33();
		
		body.setIdTipoPersona(idTipoPersona);
		
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
				responseHttp = this.client.post(this.UrlConsultaUsoCfdi33, this.headerValue, writeValueAsString);
			} catch (Exception e) {
				logger.error("Consulta uso de CFDI por regimen fiscal: " + writeValueAsString, e);
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
					logger.error("Consulta uso de CFDI por regimen fiscal: " + writeValueAsString, e);
				}
			}
			contador += 1;

		} while (responseHttp == null && contador <= 4);
		
		if (response.getRespuesta() == null) {
			UtilsApi.setRespuesta(response, ECodigo.Error.getValor());
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
			UtilsApi.setRespuesta(response, ECodigo.Error.getValor());
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
			UtilsApi.setRespuesta(response, ECodigo.Error.getValor());
		}
		return response;
	}
}

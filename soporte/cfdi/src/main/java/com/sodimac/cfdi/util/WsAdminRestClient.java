package com.sodimac.cfdi.util;

import java.util.List;
import java.util.Map;


import org.springframework.web.client.RestTemplate;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.mashape.unirest.http.Headers;
import com.mashape.unirest.http.HttpResponse;
import com.mashape.unirest.http.Unirest;
import com.sodimac.cfdi.cliente.wsadministracion.ClientResponseTYPE;
import com.sodimac.cfdi.cliente.wsadministracion.LoginReq;
import com.sodimac.cfdi.cliente.wsadministracion.ResponseBaseDto;

public class WsAdminRestClient {

	static RestTemplate restTemplate = new RestTemplate();
	
	public static <T> ClientResponseTYPE<T> get(Map<String, String> map, T entity) {
		
		HttpResponse<String> responseHttp = null;
		ObjectMapper objectMapper = new ObjectMapper();
		ClientResponseTYPE<T> response = new ClientResponseTYPE<T>();
		
		ClientResponseTYPE<String>  Authorization = login(map);
		if(!Authorization.getRespuesta().getCodigo().equals("1") ) {
			Authorization.getRespuesta().setDescripcion("Error en la obtencion del token:" + response.getRespuesta().getDescripcion());
			return response;
		}
		//String URL_REQUEST = map.get("URL_REQUEST");
		String URL_REQUEST = "http://localhost:8080/finanzasadminfacturacion/api/catologo/confdatosemisortienda/findAll";
		

		try {
			responseHttp = Unirest.get(URL_REQUEST)
				.header("Authorization", Authorization.getData())
				.header("Content-Type", "application/json")
				.asString();		
		} catch (Exception e) {
			response.getRespuesta().setCodigo("0");
			response.getRespuesta().setDescripcion("ERROR en la peticion: " + e.getMessage());
			return response;
		}
		
		if (responseHttp != null) {
			try {
				response = objectMapper.readValue(responseHttp.getBody(), new TypeReference<ClientResponseTYPE<T>>() {});
			} catch (JsonProcessingException e) {
				response.getRespuesta().setCodigo("0");
				response.getRespuesta().setDescripcion("ERROR: " + e.getMessage());
			} catch (Exception e) {
				response.getRespuesta().setCodigo("0");
				response.getRespuesta().setDescripcion("ERROR: " + e.getMessage());
			}
			
		}
		return response;
	}
	
	public static <T> ClientResponseTYPE<T> post(Map<String, String> map, T entity, Object entitye) {
		HttpResponse<String> responseHttp = null;
		ObjectMapper objectMapper = new ObjectMapper();
		ClientResponseTYPE<T> response = new ClientResponseTYPE<T>();
		
		ClientResponseTYPE<String>  Authorization = login(map);
		if(!Authorization.getRespuesta().getCodigo().equals("1") ) {
			Authorization.getRespuesta().setDescripcion("Error en la obtencion del token:" + response.getRespuesta().getDescripcion());
			return response;
		}
		//String URL_REQUEST = map.get("URL_REQUEST");
		String URL_REQUEST = "http://localhost:8080/finanzasadminfacturacion/api/catologo/confdatosemisortienda/create";

		try {
			responseHttp = Unirest.post(URL_REQUEST)
				.header("Authorization", Authorization.getData())
				.header("Content-Type", "application/json")
				.body(objectMapper.writeValueAsString(entitye) )
				.asString();		
		} catch (Exception e) {
			response.getRespuesta().setCodigo("0");
			response.getRespuesta().setDescripcion("ERROR en la peticion: " + e.getMessage());
			return response;
		}
		
		if (responseHttp != null) {
			try {
				response = objectMapper.readValue(responseHttp.getBody(), new TypeReference<ClientResponseTYPE<T>>() {});
			} catch (JsonProcessingException e) {
				response.getRespuesta().setCodigo("0");
				response.getRespuesta().setDescripcion("ERROR: " + e.getMessage());
			} catch (Exception e) {
				response.getRespuesta().setCodigo("0");
				response.getRespuesta().setDescripcion("ERROR: " + e.getMessage());
			}
			
		}
		return response;
	}
	
	public static ClientResponseTYPE<String> login(Map<String, String> map) {
	//	String URL_LOGIN = map.get("URL_LOGIN");
//		String USERNAME = map.get("USERNAME");
//		String PASSWORD = map.get("PASSWORD");
		String URL_LOGIN = "http://localhost:8080/finanzasadminfacturacion/api/login";
		String USERNAME = "userWsft";
		String PASSWORD = "E0R9KWF482";
		LoginReq loginReq = new LoginReq(USERNAME, PASSWORD);
		ClientResponseTYPE<String> response = new ClientResponseTYPE<String>();
		response.setRespuesta(new ResponseBaseDto());
		
		
		HttpResponse<String> responseHttp = null;
		ObjectMapper objectMapper = new ObjectMapper();
		//ClientResponseTYPE<List<ConfDatosEmisorTiendaDtoVM>> response = new ClientResponseTYPE<List<ConfDatosEmisorTiendaDtoVM>>(new ArrayList<ConfDatosEmisorTiendaDtoVM>());

		try {
			responseHttp = Unirest.post(URL_LOGIN)
				.header("Content-Type", "application/json")
				.body(objectMapper.writeValueAsString(loginReq) )
				.asString();		
		} catch (Exception e) {
			response.getRespuesta().setCodigo("0");
			response.getRespuesta().setDescripcion("ERROR: " + e.getMessage());
			return response;
		}
		
		
		if (responseHttp != null) {
			Headers headers = responseHttp.getHeaders();
			List<String> AUTORIZATION = headers.get("authorization");
			if(AUTORIZATION != null) {
				response.setData(AUTORIZATION.get(0));
				response.getRespuesta().setCodigo("1");
				response.getRespuesta().setDescripcion("OK");
			} else {
				response.getRespuesta().setCodigo("0");
				response.getRespuesta().setDescripcion("ERROR: No se pudo obtener el token");
			}
			
		}
		
//		try {
//			// Data attached to the request.
//			HttpEntity<LoginReq> requestBody = new HttpEntity<>(loginReq);
//			ResponseEntity<String> responseEntity = restTemplate.postForEntity(URL_LOGIN, requestBody, String.class);
//			if (responseEntity.getStatusCode() == HttpStatus.OK) {
//				String e = responseEntity.getBody();
//				HttpHeaders headers = responseEntity.getHeaders();
//				List<String> AUTORIZATION = headers.get("Authorization");
//				if(AUTORIZATION != null) {
//					Authorization = AUTORIZATION.get(0);
//				}
//			}
//		} catch (Exception e) {
//			// TODO Auto-generated catch block
//			e.printStackTrace();
//		}
		
		return response;
		
	}
	
}

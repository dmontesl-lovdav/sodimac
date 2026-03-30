package com.sodimac.oc.batch.client;

import java.util.HashMap;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

import com.sodimac.oc.batch.dto.sodimac.LoginRequest;
import com.sodimac.oc.batch.dto.sodimac.ParametroResponse;

@Component
public class SodimacParametrosClient {

	private Logger logger = LoggerFactory.getLogger(DetecnoClient.class);

	@Autowired
	private RestTemplate restTemplate;

	@Value("${api.sodimac.login.username}")
	private String USERNAME;

	@Value("${api.sodimac.login.password}")
	private String PASSWORD;

	@Value("${api.sodimac.parametros.login.url}")
	private String URL_LOGIN;

	@Value("${api.sodimac.parametros.catalogos.url}")
	private String URL_PARAMETROS;
	
	@Value("${batch}")
	private Boolean isBatch;

	public Map<String, String> getFechasParametros() {
		
		if(isBatch) {
			return null;
		}
		
		logger.info("Se obtienen parametros del API de Sodimac");
		
		Map<String, String> map = new HashMap<String, String>();

		map.put("fechaInicial", obtenerValorParametro("ConsultaOrdenesCompra.FechaInicio"));
		map.put("fechaFinal", obtenerValorParametro("ConsultaOrdenesCompra.FechaFin"));

		return map;
	}

	private String obtenerValorParametro(String parametro) {
		
		HttpHeaders headers = new HttpHeaders();
		String token = getToken();
		headers.set(HttpHeaders.AUTHORIZATION, token);
		
		HttpEntity<Object> request = new HttpEntity<>(headers);
		
		try {
			ResponseEntity<ParametroResponse> responseFechaFinal = restTemplate.exchange(URL_PARAMETROS + parametro, HttpMethod.GET, request, ParametroResponse.class);
			if (responseFechaFinal.hasBody()) {
				ParametroResponse data = responseFechaFinal.getBody();
				if (data != null && data.getData() != null) {
					return data.getData().getValor();
				} else {
					throw new RuntimeException("La peticion fue fallida o no hay data");
				}
			} else {
				throw new RuntimeException("La peticion llego sin cuerpo de respuesta");
			}
		} catch (Exception e) {
			throw new RuntimeException("Ocurrio un problema al obtener parametros de API Sodimac", e);
		}
	}

	private String getToken() {
//		logger.info("Consumiendo API Login: {}", URL_LOGIN);

		HttpHeaders headers = new HttpHeaders();
		headers.setContentType(MediaType.APPLICATION_JSON);

		LoginRequest body = new LoginRequest();
		body.setUsername(USERNAME);
		body.setPassword(PASSWORD);

		HttpEntity<LoginRequest> request = new HttpEntity<LoginRequest>(body, headers);

		try {
			ResponseEntity<String> response = restTemplate.exchange(URL_LOGIN, HttpMethod.POST, request, String.class);

			if (response.getHeaders().containsKey("authorization")) {
				return response.getHeaders().get("authorization").get(0);
			} else {
				throw new RuntimeException("La peticion llego sin token de autorizacion");
			}
		} catch (Exception e) {
			throw new RuntimeException("Ocurrio un problema al obtener token de API Sodimac", e);
		}
	}
}

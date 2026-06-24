package com.sodimac.oc.batch.client;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
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
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

import com.sodimac.oc.batch.dto.detecno.AuthToken;
import com.sodimac.oc.batch.dto.detecno.DetecnoResponse;

@Component
public class DetecnoClient {

	private Logger logger = LoggerFactory.getLogger(DetecnoClient.class);

	@Autowired
	private RestTemplate restTemplate;

	@Autowired
	SodimacParametrosClient parametrosClient;

	@Value("${api.detecno.auth.username}")
	private String USERNAME;

	@Value("${api.detecno.auth.password}")
	private String PASSWORD;

	@Value("${api.detecno.auth.url}")
	private String URL_TOKEN;

	@Value("${api.detecno.consulta-ordenes.url}")
	private String URL_CONSULTA;

	private final SimpleDateFormat format = new SimpleDateFormat("yyyy/MM/dd");

	public DetecnoResponse getOrdenesCompra() {
		return executeConsulta(createUriString());
	}

	// Overload para descarga por periodos: consulta un rango explicito (formato yyyy/MM/dd).
	public DetecnoResponse getOrdenesCompra(String fechaInicio, String fechaFin) {
		UriComponentsBuilder uriBuilder = UriComponentsBuilder.fromUriString(URL_CONSULTA);
		logger.info("Se consultan dias del: {} al {}", fechaInicio, fechaFin);
		uriBuilder.queryParam("fechainicio", fechaInicio);
		uriBuilder.queryParam("fechaFin", fechaFin);
		return executeConsulta(uriBuilder.toUriString());
	}

	private DetecnoResponse executeConsulta(String url) {

		HttpHeaders headers = new HttpHeaders();
		String token = getAccessToken();
		headers.setBearerAuth(token);

		logger.info("Consumiendo Colsulta Ordenes: {}", URL_CONSULTA);

		HttpEntity<Object> request = new HttpEntity<>(headers);

		try {
			ResponseEntity<DetecnoResponse> response = restTemplate.exchange(url, HttpMethod.GET, request, DetecnoResponse.class);

			if (response.hasBody()) {
				DetecnoResponse data = response.getBody();
				if (data != null && data.getData() != null) {
					return data;
				} else {
					throw new RuntimeException("La peticion fue fallida o no hay data");
				}
			} else {
				throw new RuntimeException("La peticion llego sin cuerpo de respuesta");
			}

		} catch (Exception e) {
			throw new RuntimeException(e);
		}
	}

	private String createUriString() {

		UriComponentsBuilder uriBuilder = UriComponentsBuilder.fromUriString(URL_CONSULTA);

		Map<String, String> parametros = parametrosClient.getFechasParametros();

		if (parametros != null && parametros.get("fechaInicial") != null && parametros.get("fechaFinal") != null) {
			// PARA UN RANGO DE FECHAS ESPECIFICO
			String fechaInicio = parametros.get("fechaInicial");
			String fechaFin = parametros.get("fechaFinal");
			logger.info("Se consultan dias del: {} al {}", fechaInicio, fechaFin);
			uriBuilder.queryParam("fechainicio", fechaInicio);
			uriBuilder.queryParam("fechaFin", fechaFin);
		} else {
			Calendar cal = Calendar.getInstance();
			cal.add(Calendar.DAY_OF_YEAR, -1);
			Date yesterday = cal.getTime();
			
			if (Calendar.getInstance().get(Calendar.DAY_OF_WEEK) == Calendar.SUNDAY) { // Si es domingo consulta una semana atras
				cal.add(Calendar.DAY_OF_YEAR, -6);
				Date lastWeek = cal.getTime();

				logger.info("Se consultan dias del: {} al {} - formato: {} - {}", lastWeek, yesterday, format.format(lastWeek), format.format(yesterday));

				uriBuilder.queryParam("fechainicio", format.format(lastWeek));
				uriBuilder.queryParam("fechaFin", format.format(yesterday));
			} else { // Si es otro dia consulta el dia anterior
				logger.info("Se consulta día: {} - formato: {}", yesterday, format.format(yesterday));

				uriBuilder.queryParam("fechainicio", format.format(yesterday));
				uriBuilder.queryParam("fechaFin", format.format(yesterday));
			}
		}

		return uriBuilder.toUriString();
	}

	private String getAccessToken() {
		logger.info("Consumiendo Auth Token: {}", URL_TOKEN);

		HttpHeaders headers = new HttpHeaders();
		headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);

		MultiValueMap<String, String> body = new LinkedMultiValueMap<>();
		body.add("username", USERNAME);
		body.add("password", PASSWORD);

		HttpEntity<MultiValueMap<String, String>> request = new HttpEntity<>(body, headers);

		try {
			ResponseEntity<AuthToken> response = restTemplate.exchange(URL_TOKEN, HttpMethod.POST, request, AuthToken.class);

			if (response.hasBody()) {

				AuthToken authToken = response.getBody();

				if (authToken.isSuccess() && authToken.getAccTkn() != null) {
					return authToken.getAccTkn();
				} else {
					throw new RuntimeException("La peticion fue fallida o el token llego nulo");
				}
			} else {
				throw new RuntimeException("La peticion llego sin cuerpo de respuesta");
			}
		} catch (Exception e) {
			throw new RuntimeException(e);
		}

	}

}

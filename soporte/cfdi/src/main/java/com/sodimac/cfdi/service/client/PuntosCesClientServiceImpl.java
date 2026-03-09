package com.sodimac.cfdi.service.client;

import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import com.sodimac.cfdi.model.puntosces.CatalogoDto;
import com.sodimac.cfdi.model.puntosces.LoginDto;
import com.sodimac.cfdi.model.puntosces.PolizaContableDto;
import com.sodimac.cfdi.model.puntosces.PolizasFilterDto;
import com.sodimac.cfdi.model.puntosces.ResponseListCatalogos;
import com.sodimac.cfdi.model.puntosces.ResponseListPolizas;
import com.sodimac.cfdi.model.puntosces.ResponsePoliza;
import com.sodimac.cfdi.model.puntosces.ResponsePoliza2;

@Service
public class PuntosCesClientServiceImpl implements PuntosCesClientService {

	Logger logger = LoggerFactory.getLogger(PuntosCesClientServiceImpl.class);
	
	RestTemplate restTemplate = new RestTemplate();
	
	@Value("${api.puntos.ces.login.username}")
	private String USERNAME;

	@Value("${api.puntos.ces.login.password}")
	private String PASSWORD;

	@Value("${api.puntos.ces.login.url}")
	private String URL_LOGIN;
	
	@Value("${api.puntos.ces.polizas.get.url}")
	private String URL_GET_POLIZAS;
	
	@Value("${api.puntos.ces.polizas.save.url}")
	private String URL_SAVE_POLIZAS;
	
	@Value("${api.puntos.ces.polizas.update.url}")
	private String URL_UPDATE_POLIZAS;
	
	@Value("${api.puntos.ces.polizas.delete.url}")
	private String URL_DELETE_POLIZAS;
	
	@Value("${api.puntos.ces.catalogos.get}")
	private String URL_GET_CATALOGO;
	
	@Value("${api.puntos.ces.sucursales.get}")
	private String URL_GET_SUCURSALES;
	
	@Override
	public List<CatalogoDto> getCatalogo(Integer idCatalogo) {
		String url = URL_GET_CATALOGO + "/" + String.valueOf(idCatalogo);
		HttpEntity<CatalogoDto> request = new HttpEntity<>(createHeader());
		
		try {
			ResponseEntity<ResponseListCatalogos> response = restTemplate.exchange(url, HttpMethod.GET, request, ResponseListCatalogos.class);
			
			return response.getBody().getData();
			
		} catch (Exception e) {
			logger.error("Ocurrio un error al traer los catalogos", e);
			throw new RuntimeException("No se pudo obtener el catalogo especificado: " + idCatalogo, e); 
		}
	}
	
	@Override
	public List<CatalogoDto> getSucursales() {
		HttpEntity<CatalogoDto> request = new HttpEntity<>(createHeader());
		
		try {
			ResponseEntity<ResponseListCatalogos> response = restTemplate.exchange(URL_GET_SUCURSALES, HttpMethod.GET, request, ResponseListCatalogos.class);
			
			return response.getBody().getData();
			
		} catch (Exception e) {
			logger.error("Ocurrio un error al traer las sucursales", e);
			throw new RuntimeException("No se pudo obtener el catalogo de sucursales" , e); 
		}
	}
	
	@Override
	public List<PolizaContableDto> getPolizasContablesFilter(PolizasFilterDto polizasFilter) {
		HttpEntity<PolizasFilterDto> request = new HttpEntity<>(polizasFilter, createHeader());
		
		try {
			logger.info("Consumiendo Get Polizas Filter: {}", URL_GET_POLIZAS);
			
			ResponseEntity<ResponseListPolizas> response = restTemplate.exchange(URL_GET_POLIZAS, HttpMethod.POST, request, ResponseListPolizas.class);
			
			return response.getBody().getData();
		} catch (Exception e) {
			logger.error("Ocurrio un error al traer las polizas", e);
			throw new RuntimeException("No se pudo obtener las polizas en PuntosCes", e); 
		}
	}

	
	@Override
	public PolizaContableDto getPolizaContable(Integer idConfigContable) {
		String url = URL_GET_POLIZAS + "/" + idConfigContable;
		
		HttpEntity<String> request = new HttpEntity<>(createHeader());
		
		try {
			logger.info("Consumiendo Get Poliza: {}", url);
			
			ResponseEntity<ResponsePoliza> response = restTemplate.exchange(url, HttpMethod.GET, request, ResponsePoliza.class);
			
			return response.getBody().getData();
		} catch (Exception e) {
			logger.error("Ocurrio un error al traer la poliza especificada", e);
			throw new RuntimeException("No se pudo obtener la poliza especificada en PuntosCes", e); 
		}
	}
	
	
	@Override
	public void savePolizaContable(PolizaContableDto polizaContable) {
		HttpEntity<PolizaContableDto> request = new HttpEntity<>(polizaContable, createHeader());
		
		try {
			logger.info("Consumiendo Registrar Poliza: {}", URL_SAVE_POLIZAS);
			
			restTemplate.exchange(URL_SAVE_POLIZAS, HttpMethod.POST, request, ResponsePoliza.class);
		} catch (Exception e) {
			logger.error("Ocurrio un error al guardar la poliza", e);
			throw new RuntimeException("No se pudo registrar la poliza en PuntosCes", e); 
		}
	}
	
	@Override
	public void updatePolizaContable(PolizaContableDto polizaContable) {
		HttpEntity<PolizaContableDto> request = new HttpEntity<>(polizaContable, createHeader());
		
		try {
			logger.info("Consumiendo Actualizar Poliza: {}", URL_UPDATE_POLIZAS);
			
			restTemplate.exchange(URL_UPDATE_POLIZAS, HttpMethod.PUT, request, ResponsePoliza.class);
		} catch (Exception e) {
			logger.error("Ocurrio un error al actualizar la poliza", e);
			throw new RuntimeException("No se pudo actualizar la poliza en PuntosCes", e); 
		}
	}
	
	@Override
	public void deletePolizaContable(Integer idConfigContable) {
		String url = URL_DELETE_POLIZAS + "/" + idConfigContable;
		
		HttpEntity<String> request = new HttpEntity<>(createHeader());
		
		try {
			logger.info("Consumiendo Eliminar Poliza: {}", url);
			
			restTemplate.exchange(url, HttpMethod.POST, request, ResponsePoliza2.class);
		} catch (Exception e) {
			logger.error("Ocurrio un error al eliminar la poliza", e);
			throw new RuntimeException("No se pudo eliminar la poliza especificada en PuntosCes", e); 
		}
	}

	private HttpHeaders createHeader() {
		HttpHeaders headers = new HttpHeaders();
		headers.set("Authorization", getAuthToken());		
		return headers;
	}

	private String getAuthToken() {
//		logger.info("Consumiendo Auth Token: {}", URL_LOGIN);
		
		LoginDto loginDto = new LoginDto(USERNAME, PASSWORD);
		
		HttpEntity<LoginDto> request = new HttpEntity<>(loginDto);

		try {
			ResponseEntity<String> response = restTemplate.exchange(URL_LOGIN, HttpMethod.POST, request, String.class);
			
			return response.getHeaders().get("authorization").get(0);
		} catch (Exception e) {
			throw new RuntimeException("No se pudo realizar la autenticación en PuntosCes", e);
		}
	}
	
}

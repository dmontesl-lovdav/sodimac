package com.sodimac.facturacion.clientews.configuracion;

import com.mashape.unirest.http.HttpResponse;
import com.sodimac.facturacion.exception.ClientException;

public interface ConfiguracionClient {
	
	public HttpResponse<String> login(String url, String usuario, String password) throws ClientException;
	
	public HttpResponse<String> post(String url, String headerValue, String body) throws ClientException;
	
}

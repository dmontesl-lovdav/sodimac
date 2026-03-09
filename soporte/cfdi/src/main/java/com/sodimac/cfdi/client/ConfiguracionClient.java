package com.sodimac.cfdi.client;

import com.mashape.unirest.http.HttpResponse;
import com.sodimac.cfdi.ClientException;

public interface ConfiguracionClient {

	public HttpResponse<String> login(String url, String usuario, String password) throws ClientException;
	
	public HttpResponse<String> post(String url, String headerValue, String body) throws ClientException;
	
	public HttpResponse<String> get(String url, String headerValue) throws ClientException;
}

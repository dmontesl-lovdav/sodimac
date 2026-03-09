package com.sodimac.cfdi.cliente.wsadministracion.service;


import org.springframework.stereotype.Service;

import com.sodimac.cfdi.cliente.wsadministracion.ClientResponseTYPE;
import com.sodimac.cfdi.cliente.wsadministracion.ConfDatosEmisorTiendaDtoVM;
import com.sodimac.cfdi.util.WsAdminRestClient;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.annotation.PostConstruct;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


@Service
public class WsAdminRestClientServiceImpl implements WsAdminRestClientService {

	Logger logger = LoggerFactory.getLogger(WsAdminRestClientServiceImpl.class);
	
	private String URL_WSADMIN;
	private String URL_WSADMIN_LOGIN ;
	
	private Map<String, String> map;
	
	@PostConstruct
	private void inicializar() {
		map = new HashMap<String, String>();
		URL_WSADMIN_LOGIN = URL_WSADMIN + URL_WSADMIN_LOGIN;
		map.put("URL_WSADMIN_LOGIN", URL_WSADMIN_LOGIN);
	}

	@Override
	public <T> ClientResponseTYPE<T> get(String KEY, T entitya) {
		String URL_WSADMIN_REQUEST = obtieneUrlRequest(KEY);
		map.put("URL_REQUEST", URL_WSADMIN_REQUEST);
		ClientResponseTYPE<T> responseGet = WsAdminRestClient.get(map,  entitya);
		return responseGet;
	}

	@Override
	public <T> ClientResponseTYPE<T> post(String KEY, T entitya, Object obj) {
		String URL_WSADMIN_REQUEST = obtieneUrlRequest(KEY);
		map.put("URL_REQUEST", URL_WSADMIN_REQUEST);
		ClientResponseTYPE<T> responsePost = WsAdminRestClient.post(map,  entitya, obj);
		return responsePost;
	}
	
	private String obtieneUrlRequest(String KEY) {
		
		return "";
	}
}

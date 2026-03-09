package com.sodimac.cfdi.cliente.wsadministracion.service;

import com.sodimac.cfdi.cliente.wsadministracion.ClientResponseTYPE;

public interface WsAdminRestClientService {

	public <T> ClientResponseTYPE<T> get(String KEY, T entity);
	
	public <T> ClientResponseTYPE<T> post(String KEY, T entity, Object obj);
}

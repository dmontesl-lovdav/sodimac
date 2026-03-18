package com.sodimac.facturacion.service;

import java.util.List;

import com.sodimac.facturacion.cliente.ws.model.ClientResponseTYPE;
import com.sodimac.facturacion.models.Mes;
import com.sodimac.facturacion.models.Periodicidad;
import com.sodimac.facturacion.models.UsoDeCfdi;

public interface ConfiguracionService {

	public String obtenerToken(String url, String usuario, String password);
	
	public ClientResponseTYPE<Mes> consultarMes(String clave);
	
	public ClientResponseTYPE<Periodicidad> consultarPeriodicidad(String clave);
	
	public ClientResponseTYPE<List<UsoDeCfdi>> consultarUsoCfdi33All();

	public ClientResponseTYPE<List<UsoDeCfdi>> consultarUsoCfdi40All();
}

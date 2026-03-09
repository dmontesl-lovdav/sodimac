package com.sodimac.cfdi.service;

import java.util.List;

import com.sodimac.cfdi.cliente.wsadministracion.CatCodigoPostalDtoVM;
import com.sodimac.cfdi.cliente.wsadministracion.CatTipoTiendaDtoVM;
import com.sodimac.cfdi.cliente.wsadministracion.ConfDatosEmisorDtoVM;
import com.sodimac.cfdi.cliente.wsadministracion.ConfDatosEmisorTiendaDtoVM;
import com.sodimac.cfdi.model.ClientResponseTYPE;
import com.sodimac.cfdi.models.UsoDeCfdi;

public interface ConfiguracionService {

	public String obtenerToken(String url, String usuario, String password);
	
	public ClientResponseTYPE<List<UsoDeCfdi>> consultarUsoCfdi33All();

	public ClientResponseTYPE<List<UsoDeCfdi>> consultarUsoCfdi40All();
	
	public ClientResponseTYPE<List<ConfDatosEmisorTiendaDtoVM>> consultaConfDatosEmisorTiendaAll();
	
	public ClientResponseTYPE<String> consultaConfDatosEmisorTiendaUpdate(ConfDatosEmisorTiendaDtoVM body);
	
	public ClientResponseTYPE<String> consultaConfDatosEmisorTiendaCreate(ConfDatosEmisorTiendaDtoVM body);
	
	public ClientResponseTYPE<List<CatTipoTiendaDtoVM>> catTipoTiendaFindAll();
	
	public ClientResponseTYPE<List<ConfDatosEmisorDtoVM>> consultaConfDatosEmisorAll();
	
	public ClientResponseTYPE<CatCodigoPostalDtoVM> consultaCodigoPostalById(Integer cp);
	
}

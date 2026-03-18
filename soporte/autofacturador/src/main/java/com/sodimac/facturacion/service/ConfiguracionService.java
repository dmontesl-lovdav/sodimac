package com.sodimac.facturacion.service;

import java.util.List;

import com.sodimac.facturacion.clientews.model.ClientResponseTYPE;
import com.sodimac.facturacion.models.CodigoPostal;
import com.sodimac.facturacion.models.ListaRegimenFiscal;
import com.sodimac.facturacion.models.RegimenCapital;
import com.sodimac.facturacion.models.UsoDeCfdi;
import com.sodimac.facturacion.models.VersionCfdi;

public interface ConfiguracionService {

	public String obtenerToken(String url, String usuario, String password);
	
	public ClientResponseTYPE<CodigoPostal> consultarCodigoPostal(String cp);
	
	public ClientResponseTYPE<ListaRegimenFiscal> consultarRegimenFiscal(Integer idTipoPersona);

	public ClientResponseTYPE<VersionCfdi> consultarVersionCFDI(Integer idAplicacion);

	public ClientResponseTYPE<RegimenCapital> validarRegimenCapital(String razonSocial);

	public List<UsoDeCfdi> consultarUsoCfdiInstance();
	
	public ClientResponseTYPE<List<UsoDeCfdi>> consultarUsoCfdi();

	public ClientResponseTYPE<List<UsoDeCfdi>> consultarUsoCfdi40(Integer idTipoPersona, String regimenFiscal);
	
	public ClientResponseTYPE<List<UsoDeCfdi>> consultarUsoCfdi33(Integer idTipoPersona);

	public ClientResponseTYPE<List<UsoDeCfdi>> consultarUsoCfdi33All();

	public ClientResponseTYPE<List<UsoDeCfdi>> consultarUsoCfdi40All();
	
}

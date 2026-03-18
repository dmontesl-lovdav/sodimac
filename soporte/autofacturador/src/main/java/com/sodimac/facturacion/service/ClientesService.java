package com.sodimac.facturacion.service;

import com.sodimac.facturacion.entity.ClientesEntity;

public interface ClientesService {
	
	public boolean validarRZExpresionRegular (String razonSocial);
	public boolean validarRfcExpresionRegular(String rfc);
	public boolean validarEmailExpresionRegular(String email);
	public boolean validarObraExpresionRegular (String nombreObra);
	public boolean validarResponsableObraExpresionRegular (String responsableObra);
	public int existRfcFactura(String rfc);
	public ClientesEntity getCliente(String rfc);
	public String getInformacionCliente(String rfc);
	public int inicializarRfcTicket(String rfc, String ticket);
	public void saveClientes(ClientesEntity model);
	public boolean isExistRfc(String rfc);
	public String validarCodigoPostal(String codigoPostal);
	public String consultarVersionCFDI(Integer idAplicacion);
	public String validarRegimenCapital(String razonSocial);
	
	public String consultarVersionCFDIEspecifico(String ticket, String version, String vvee);
	
	public String getInformacionCliente(String rfc, String versionCfdi);
	
	public String consultarUsoCfdiVersion(Integer idTipoPersona, Integer idVersionCfdi, String regimenFiscal);
	
}

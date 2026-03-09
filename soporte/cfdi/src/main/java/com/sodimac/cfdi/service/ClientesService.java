package com.sodimac.cfdi.service;

import com.sodimac.cfdi.entity.fiscal.ClientesEntity;

public interface ClientesService {
	
	public boolean validarRZExpresionRegular (String razonSocial);
	
	public boolean validarRfcExpresionRegular(String rfc);
	
	public boolean validarEmailExpresionRegular(String email);
	
	public boolean validarObraExpresionRegular (String nombreObra);
	
	public boolean validarResponsableObraExpresionRegular (String responsableObra);
	
	public ClientesEntity getCliente(String rfc);
	
	public void saveClientes(ClientesEntity model);
	
	public boolean isExistRfc(String rfc);
}

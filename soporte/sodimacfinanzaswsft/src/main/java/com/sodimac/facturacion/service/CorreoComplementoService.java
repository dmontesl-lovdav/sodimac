package com.sodimac.facturacion.service;

import com.sodimac.facturacion.cliente.BodyComplementoCorreoTYPE;

public interface CorreoComplementoService {

	public boolean enviarCorreoComplemento(BodyComplementoCorreoTYPE model);
	
	public boolean enviarCorreoFactura(BodyComplementoCorreoTYPE model);
	
}

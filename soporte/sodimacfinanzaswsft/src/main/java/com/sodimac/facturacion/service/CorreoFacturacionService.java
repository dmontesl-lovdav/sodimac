package com.sodimac.facturacion.service;

import com.sodimac.facturacion.models.ClientesTemporalModel;

public interface CorreoFacturacionService {

	public boolean enviarCorreo(ClientesTemporalModel model);

	public void enviarCorreoAsincrono(ClientesTemporalModel model);
	
	public boolean enviarTokenMultiple(ClientesTemporalModel model);
	
}

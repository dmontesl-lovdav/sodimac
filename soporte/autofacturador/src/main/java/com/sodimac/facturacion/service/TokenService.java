package com.sodimac.facturacion.service;

import com.sodimac.facturacion.entity.ConfiguracionTokenEntity;

public interface TokenService {
	
	public String GenerarToken(String sessionId, String rfc, String email);
	public boolean validarExpresionRegular(String token);
	public int existToken(String sessionId, String token, String rfc, String email);
	public ConfiguracionTokenEntity getConfiguracion();;
}

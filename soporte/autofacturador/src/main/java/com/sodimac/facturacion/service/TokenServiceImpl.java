package com.sodimac.facturacion.service;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.sodimac.facturacion.entity.ConfiguracionTokenEntity;
import com.sodimac.facturacion.repository.TokenRepository;

@Service
public class TokenServiceImpl implements TokenService {

	@Autowired
	private TokenRepository tokenRepository;
	@Autowired
	private SeguridadService seguridadService;
		
	@Transactional
	public String GenerarToken(String sessionId, String rfc, String email) {
		return tokenRepository.generarToken(sessionId, seguridadService.encriptar(rfc), seguridadService.encriptar(email));
	}
	
	@Transactional
	public boolean validarExpresionRegular(String token) {
		boolean result = true;
		
		if (token.isEmpty()) {
			result = false;
		} else {
			
			ConfiguracionTokenEntity configuracion = getConfiguracion();
			
			if (configuracion!=null) {
				String expresionRegularToken = "";
				int longitud = 0;
				
				longitud = configuracion.getLongitud();
				if (token.length()> longitud) {
					result = false;
				} else {
					if (configuracion.isMayusculas()) {
						expresionRegularToken += "A-Z";
					}
					if (configuracion.isMinusculas()) {
						expresionRegularToken += "a-z";
					}
					if (configuracion.isNumeros()) {
						expresionRegularToken += "0-9";
					}

					expresionRegularToken = "^[" + expresionRegularToken + "]*$";
					Pattern pat = Pattern.compile(expresionRegularToken);
			        Matcher mat = pat.matcher(token);
			        if (!mat.matches()) {
			        	result = false;
			        }					
					
				}				
			}
			
		}
		return result;
	}
	
	@Transactional
	public int existToken(String sessionId, String token, String rfc, String email) {
		return tokenRepository.existToken(sessionId, token, seguridadService.encriptar(rfc), seguridadService.encriptar(email));
	}
	
	@Transactional
	public ConfiguracionTokenEntity getConfiguracion() {
		return tokenRepository.findFirstByOrderByIdConfTokenDesc();
	};
	
}

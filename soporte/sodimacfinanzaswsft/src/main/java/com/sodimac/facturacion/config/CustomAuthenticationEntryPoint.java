package com.sodimac.facturacion.config;

import java.io.IOException;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.AuthenticationEntryPoint;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.sodimac.facturacion.cliente.ClienteTicketTimbrarExpRespTYPE;
import com.sodimac.facturacion.util.UtilsApi;
import com.sodimac.facturacion.util.enums.ECodigo;

public class CustomAuthenticationEntryPoint implements AuthenticationEntryPoint {

    @Override
    public void commence(HttpServletRequest req, HttpServletResponse res, AuthenticationException authException) throws IOException, ServletException {
        
    	ClienteTicketTimbrarExpRespTYPE response = new ClienteTicketTimbrarExpRespTYPE();
    	UtilsApi.setRespuesta(response, ECodigo.AccesoDenegado.getValor());
    	
    	res.setContentType("application/json;charset=UTF-8");
        res.setStatus(HttpServletResponse.SC_FORBIDDEN);
        res.getWriter().write(new ObjectMapper().writeValueAsString(response));
    }
} 
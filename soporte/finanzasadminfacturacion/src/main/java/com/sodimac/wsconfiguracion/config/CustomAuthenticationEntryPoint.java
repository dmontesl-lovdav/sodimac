package com.sodimac.wsconfiguracion.config;

import java.io.IOException;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.AuthenticationEntryPoint;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.sodimac.wsconfiguracion.dto.ResponseErrorDto;
import com.sodimac.wsconfiguracion.util.UtilsApi;
import com.sodimac.wsconfiguracion.util.enums.ECodigo;

public class CustomAuthenticationEntryPoint implements AuthenticationEntryPoint {

    @Override
    public void commence(HttpServletRequest req, HttpServletResponse res, AuthenticationException authException) throws IOException, ServletException {
        
    	ResponseErrorDto response = new ResponseErrorDto();
    	UtilsApi.setRespuesta(response.getRespuesta(), ECodigo.AccesoDenegado);
    	
    	res.setContentType("application/json;charset=UTF-8");
        res.setStatus(HttpServletResponse.SC_FORBIDDEN);
        res.getWriter().write(new ObjectMapper().writeValueAsString(response));
    }
} 
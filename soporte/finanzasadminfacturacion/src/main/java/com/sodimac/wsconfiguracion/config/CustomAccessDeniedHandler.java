package com.sodimac.wsconfiguracion.config;

import java.io.IOException;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.web.access.AccessDeniedHandler;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.sodimac.wsconfiguracion.dto.ResponseErrorDto;
import com.sodimac.wsconfiguracion.util.UtilsApi;
import com.sodimac.wsconfiguracion.util.enums.ECodigo;

public class CustomAccessDeniedHandler implements AccessDeniedHandler {

    @Override
    public void handle(HttpServletRequest req, HttpServletResponse res, AccessDeniedException accessDeniedException) throws IOException, ServletException {

    	ResponseErrorDto response = new ResponseErrorDto();
    	UtilsApi.setRespuesta(response.getRespuesta(), ECodigo.AccesoDenegado);
    	
    	res.setContentType("application/json;charset=UTF-8");
        res.setStatus(HttpServletResponse.SC_FORBIDDEN);
        res.getWriter().write(new ObjectMapper().writeValueAsString(response));
        
    }
}

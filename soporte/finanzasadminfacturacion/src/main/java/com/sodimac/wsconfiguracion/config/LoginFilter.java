package com.sodimac.wsconfiguracion.config;


import com.auth0.jwt.JWT;
import com.auth0.jwt.algorithms.Algorithm;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.sodimac.wsconfiguracion.service.config.TokenService;

import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.authentication.AbstractAuthenticationProcessingFilter;
import org.springframework.security.web.util.matcher.AntPathRequestMatcher;

import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.InputStream;
import java.util.Collections;
import java.util.Date;


public class LoginFilter extends AbstractAuthenticationProcessingFilter {

    private final TokenService tokenService;

    public LoginFilter(String url, AuthenticationManager authManager, TokenService tokenService) {
        super(new AntPathRequestMatcher(url));
        setAuthenticationManager(authManager);
        this.tokenService = tokenService;
    }
    
    

    @Override
    public Authentication attemptAuthentication(
            HttpServletRequest req, HttpServletResponse res)
            throws AuthenticationException, IOException, ServletException {
    	
        // obtenemos el body de la peticion que asumimos viene en formato JSON
        InputStream body = req.getInputStream();

        // Asumimos que el body tendrá el siguiente JSON  {"username":"ask", "password":"123"}
        // Realizamos un mapeo a nuestra clase User para tener ahi los datos
        User user = new ObjectMapper().readValue(body, User.class);

        // Finalmente autenticamos
        // Spring comparará el user/password recibidos
        // contra el que definimos en la clase SecurityConfig
        return getAuthenticationManager().authenticate(
                new UsernamePasswordAuthenticationToken(
                        user.getUsername(),
                        user.getPassword(),
                        Collections.emptyList()
                )
        );
    }

    @Override
    protected void successfulAuthentication(
            HttpServletRequest req,
            HttpServletResponse res, FilterChain chain,
            Authentication auth) throws IOException, ServletException {

        // Si la autenticacion fue exitosa, agregamos el token a la respuesta
		String sessionId = "sesion";
		String token = tokenService.getToken(sessionId, auth.getName());

		// Configurar CORS y agregar el token
		res.setHeader("Access-Control-Expose-Headers", "Authorization");
		res.setHeader("Authorization", token);
		res.setContentType("application/json");
		res.setCharacterEncoding("UTF-8");

		// Escribir respuesta JSON
		String jsonResponse = "{\"token\":\"" + token + "\"}";
		res.getWriter().write(jsonResponse);
		res.getWriter().flush();
    }
    

}

